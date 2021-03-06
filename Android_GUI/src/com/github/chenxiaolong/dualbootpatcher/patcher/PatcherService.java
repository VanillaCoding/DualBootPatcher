/*
 * Copyright (C) 2015  Andrew Gunnerson <andrewgunnerson@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.chenxiaolong.dualbootpatcher.patcher;

import android.util.Log;

import com.github.chenxiaolong.dualbootpatcher.BuildConfig;
import com.github.chenxiaolong.dualbootpatcher.LogUtils;
import com.github.chenxiaolong.dualbootpatcher.ThreadPoolService;
import com.github.chenxiaolong.dualbootpatcher.nativelib.LibMbp.Device;
import com.github.chenxiaolong.dualbootpatcher.nativelib.LibMbp.FileInfo;
import com.github.chenxiaolong.dualbootpatcher.nativelib.LibMbp.Patcher;
import com.github.chenxiaolong.dualbootpatcher.nativelib.LibMbp.Patcher.ProgressListener;
import com.github.chenxiaolong.dualbootpatcher.nativelib.LibMbp.PatcherConfig;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PatcherService extends ThreadPoolService {
    private static final String TAG = PatcherService.class.getSimpleName();

    private static final String THREAD_POOL_DEFAULT = "default";
    private static final String THREAD_POOL_PATCHING = "patching";
    private static final int THREAD_POOL_DEFAULT_THREADS = 2;
    private static final int THREAD_POOL_PATCHING_THREADS = 2;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // Update WeakReferences in all of our tasks. This is extremely ugly, but we need to
        // preserve our tasks throughout the service lifecycle.
        try {
            mCallbacksLock.writeLock().lock();
            for (Map.Entry<Integer, PatchFileTask> entry : sPatcherTasks.entrySet()) {
                entry.getValue().setService(this);
            }
        } finally {
            mCallbacksLock.writeLock().unlock();
        }

        addThreadPool(THREAD_POOL_DEFAULT, THREAD_POOL_DEFAULT_THREADS);
        addThreadPool(THREAD_POOL_PATCHING, THREAD_POOL_PATCHING_THREADS);
    }

    /** List of callbacks for receiving events */
    private final ArrayList<PatcherEventListener> mCallbacks = new ArrayList<>();
    /** Read/write lock for callbacks. */
    private final ReentrantReadWriteLock mCallbacksLock = new ReentrantReadWriteLock();

    public void registerCallback(PatcherEventListener callback) {
        if (callback == null) {
            Log.w(TAG, "Tried to register null callback!");
            return;
        }

        try {
            mCallbacksLock.writeLock().lock();
            mCallbacks.add(callback);
        } finally {
            mCallbacksLock.writeLock().unlock();
        }
    }

    public void unregisterCallback(PatcherEventListener callback) {
        if (callback == null) {
            Log.w(TAG, "Tried to unregister null callback!");
            return;
        }

        try {
            mCallbacksLock.writeLock().lock();
            if (!mCallbacks.remove(callback)) {
                Log.w(TAG, "Callback was never registered: " + callback);
            }
        } finally {
            mCallbacksLock.writeLock().unlock();
        }
    }

    private void executeAllCallbacks(CallbackRunnable runnable) {
        try {
            mCallbacksLock.readLock().lock();
            for (PatcherEventListener cb : mCallbacks) {
                runnable.call(cb);
            }
        } finally {
            mCallbacksLock.readLock().unlock();
        }
    }

    private interface CallbackRunnable {
        void call(PatcherEventListener callback);
    }

    // TODO: Won't survive out-of-memory app restart
    private static final ReentrantReadWriteLock sPatcherTasksLock = new ReentrantReadWriteLock();
    private static final HashMap<Integer, PatchFileTask> sPatcherTasks = new HashMap<>();
    private static final AtomicInteger sPatcherNewTaskId = new AtomicInteger(0);

    // Patcher helper methods

    private void addTask(int taskId, PatchFileTask task) {
        try {
            sPatcherTasksLock.writeLock().lock();
            sPatcherTasks.put(taskId, task);
        } finally {
            sPatcherTasksLock.writeLock().unlock();
        }
    }

    private PatchFileTask getTask(int taskId) {
        try {
            sPatcherTasksLock.readLock().lock();
            return sPatcherTasks.get(taskId);
        } finally {
            sPatcherTasksLock.readLock().unlock();
        }
    }

    private PatchFileTask removeTask(int taskId) {
        try {
            sPatcherTasksLock.writeLock().lock();
            return sPatcherTasks.remove(taskId);
        } finally {
            sPatcherTasksLock.writeLock().unlock();
        }
    }

    /**
     * Asynchronously initialize the patcher
     *
     * This will:
     * - Extract the data archive
     * - Clean up old data directories
     * - Create an singleton instance of {@link PatcherConfig} in {@link PatcherUtils#sPC}
     *
     * After the task finishes, {@link PatcherEventListener#onPatcherInitialized()} will be called
     * on all registered callbacks.
     */
    public void initializePatcher() {
        InitializePatcherTask task = new InitializePatcherTask(this);
        enqueueOperation(THREAD_POOL_DEFAULT, task);
    }

    /**
     * Get list of file patching task IDs
     *
     * NOTE: This list should correspond with what is shown in the UI.
     *
     * @return Array of file patching task IDs.
     */
    public int[] getPatchFileTaskIds() {
        try {
            sPatcherTasksLock.readLock().lock();
            int[] taskIds = new int[sPatcherTasks.size()];
            int index = 0;
            for (int taskId : sPatcherTasks.keySet()) {
                taskIds[index++] = taskId;
            }
            return taskIds;
        } finally {
            sPatcherTasksLock.readLock().unlock();
        }
    }

    /**
     * Add file patching task
     *
     * The task does not execute until {@link #startPatching(int)} is called.
     *
     * @param patcherId libmbp patcher ID
     * @param path Path of file to patch
     * @param device Target device
     * @param romId Target ROM ID
     * @return Task ID for the new task
     */
    public int addPatchFileTask(String patcherId, String path, Device device, String romId) {
        int taskId = sPatcherNewTaskId.getAndIncrement();
        PatchFileTask task = new PatchFileTask(this, taskId);
        task.mPatcherId = patcherId;
        task.mPath = path;
        task.mDevice = device;
        task.mRomId = romId;
        addTask(taskId, task);
        return taskId;
    }

    /**
     * Remove file patching task
     *
     * @param taskId Task ID
     * @return Whether the task was removed
     */
    public boolean removePatchFileTask(int taskId) {
        cancelPatching(taskId);
        PatchFileTask task = removeTask(taskId);
        return task != null;
    }

    /**
     * Asynchronously start patching a file
     *
     * During patching, the following callback methods may be called:
     * - {@link PatcherEventListener#onPatcherUpdateDetails(int, String)} : Called when libmbp
     *   reports a single-line status text item (usually the file inside the archive being
     *   processed)
     * - {@link PatcherEventListener#onPatcherUpdateProgress(int, long, long)} : Called when libmbp
     *   reports the current progress value and the maximum progress value. Make no assumptions
     *   about the current and maximum progress values. It is not guaranteed that current <= maximum
     *   or that they are positive.
     * - {@link PatcherEventListener#onPatcherUpdateFilesProgress(int, long, long)} : Called when
     *   libmbp reports the current progress and maximum progress in terms of the number of files
     *   within the archive that have been processed. Make no assumptions about the current and
     *   maximum progress values. It is not guaranteed that current <= maximum or that they are
     *   positive.
     *
     * When the patching is finished,
     * {@link PatcherEventListener#onPatcherFinished(int, boolean, boolean, int, String)} will be
     * called.
     *
     * @param taskId Task ID
     */
    public void startPatching(int taskId) {
        PatchFileTask task = getTask(taskId);
        enqueueOperation(THREAD_POOL_PATCHING, task);
    }

    /**
     * Cancel file patching
     *
     * This method will attempt to cancel a patching operation in progress. The task will only be
     * cancelled if the corresponding libmbp patcher respects the cancelled flag and stops when it
     * is set. If a task has been cancelled,
     * {@link PatcherEventListener#onPatcherFinished(int, boolean, boolean, int, String)} will be
     * called in the same manner as described in {@link #startPatching(int)}. The returned error
     * code may or may not specify that the task has been cancelled.
     *
     * @param taskId Task ID
     */
    public void cancelPatching(int taskId) {
        PatchFileTask task = getTask(taskId);
        cancelOperation(THREAD_POOL_PATCHING, task);
        task.cancel();
    }

    private void enforceQueuedState(PatchFileTask task) {
        PatchFileState state = task.mState.get();
        if (state != PatchFileState.QUEUED) {
            throw new IllegalStateException(
                    "Cannot change task properties in " + state.name() + " state");
        }
    }

    public void setPatcherId(int taskId, String patcherId) {
        PatchFileTask task = getTask(taskId);
        enforceQueuedState(task);
        task.mPatcherId = patcherId;
    }

    public String getPatcherId(int taskId) {
        PatchFileTask task = getTask(taskId);
        return task.mPatcherId;
    }

    public void setPath(int taskId, String path) {
        PatchFileTask task = getTask(taskId);
        enforceQueuedState(task);
        task.mPath = path;
    }

    public String getPath(int taskId) {
        PatchFileTask task = getTask(taskId);
        return task.mPath;
    }

    public void setDevice(int taskId, Device device) {
        PatchFileTask task = getTask(taskId);
        enforceQueuedState(task);
        task.mDevice = device;
    }

    public Device getDevice(int taskId) {
        PatchFileTask task = getTask(taskId);
        return task.mDevice;
    }

    public void setRomId(int taskId, String romId) {
        PatchFileTask task = getTask(taskId);
        enforceQueuedState(task);
        task.mRomId = romId;
    }

    public String getRomId(int taskId) {
        PatchFileTask task = getTask(taskId);
        return task.mRomId;
    }

    public PatchFileState getState(int taskId) {
        PatchFileTask task = getTask(taskId);
        return task.mState.get();
    }

    public String getDetails(int taskId) {
        PatchFileTask task = getTask(taskId);
        return task.mDetails.get();
    }

    public long getCurrentBytes(int taskId) {
        PatchFileTask task = getTask(taskId);
        return task.mBytes.get();
    }

    public long getMaximumBytes(int taskId) {
        PatchFileTask task = getTask(taskId);
        return task.mMaxBytes.get();
    }

    public long getCurrentFiles(int taskId) {
        PatchFileTask task = getTask(taskId);
        return task.mFiles.get();
    }

    public long getMaximumFiles(int taskId) {
        PatchFileTask task = getTask(taskId);
        return task.mMaxFiles.get();
    }

    public boolean isSuccessful(int taskId) {
        PatchFileTask task = getTask(taskId);
        return task.mSuccessful.get();
    }

    public int getErrorCode(int taskId) {
        PatchFileTask task = getTask(taskId);
        return task.mErrorCode.get();
    }

    public String getNewPath(int taskId) {
        PatchFileTask task = getTask(taskId);
        return task.mNewPath.get();
    }

    // Patcher event dispatch methods

    public interface PatcherEventListener {
        void onPatcherInitialized();

        void onPatcherUpdateDetails(int taskId, String details);

        void onPatcherUpdateProgress(int taskId, long bytes, long maxBytes);

        void onPatcherUpdateFilesProgress(int taskId, long files, long maxFiles);

        void onPatcherStarted(int taskId);

        void onPatcherFinished(int taskId, boolean cancelled, boolean ret, int errorCode,
                               String newPath);
    }

    private void onPatcherInitialized() {
        executeAllCallbacks(new CallbackRunnable() {
            @Override
            public void call(PatcherEventListener callback) {
                callback.onPatcherInitialized();
            }
        });
    }

    private void onPatcherUpdateDetails(final int taskId, final String details) {
        executeAllCallbacks(new CallbackRunnable() {
            @Override
            public void call(PatcherEventListener callback) {
                callback.onPatcherUpdateDetails(taskId, details);
            }
        });
    }

    private void onPatcherUpdateProgress(final int taskId, final long bytes, final long maxBytes) {
        executeAllCallbacks(new CallbackRunnable() {
            @Override
            public void call(PatcherEventListener callback) {
                callback.onPatcherUpdateProgress(taskId, bytes, maxBytes);
            }
        });
    }

    private void onPatcherUpdateFilesProgress(final int taskId, final long files,
                                              final long maxFiles) {
        executeAllCallbacks(new CallbackRunnable() {
            @Override
            public void call(PatcherEventListener callback) {
                callback.onPatcherUpdateFilesProgress(taskId, files, maxFiles);
            }
        });
    }

    private void onPatcherStarted(final int taskId) {
        executeAllCallbacks(new CallbackRunnable() {
            @Override
            public void call(PatcherEventListener callback) {
                callback.onPatcherStarted(taskId);
            }
        });
    }

    private void onPatcherFinished(final int taskId, final boolean cancelled, final boolean ret,
                                   final int errorCode, final String newPath) {
        executeAllCallbacks(new CallbackRunnable() {
            @Override
            public void call(PatcherEventListener callback) {
                callback.onPatcherFinished(taskId, cancelled, ret, errorCode, newPath);
            }
        });
    }

    // Patcher task

    private static abstract class BaseTask implements Runnable {
        /**
         * This is set in {@link #onCreate()}. The service should never go away while the task is
         * running.
         */
        private WeakReference<PatcherService> mService;

        public BaseTask(PatcherService service) {
            mService = new WeakReference<>(service);
        }

        public PatcherService getService() {
            return mService.get();
        }

        public void setService(PatcherService service) {
            mService = new WeakReference<>(service);
        }
    }

    private static final class PatchFileTask extends BaseTask implements ProgressListener {
        /** Task ID */
        private final int mTaskId;
        /** libmbp {@link Patcher} object */
        private Patcher mPatcher;
        /** Whether this task has already been executed */
        private boolean mExecuted;

        // Patching information

        /** Patcher ID for creating {@link #mPatcher} */
        String mPatcherId;
        /** Path to file to patch */
        String mPath;
        /** Target {@link Device} */
        Device mDevice;
        /** Target ROM ID */
        String mRomId;

        // State information

        /** Patching state */
        AtomicReference<PatchFileState> mState = new AtomicReference<>(PatchFileState.QUEUED);
        /** Whether the patching was cancelled */
        AtomicBoolean mCancelled = new AtomicBoolean(false);
        /** Details text */
        AtomicReference<String> mDetails = new AtomicReference<>();
        /** Current bytes processed */
        AtomicLong mBytes = new AtomicLong(0);
        /** Maximum bytes */
        AtomicLong mMaxBytes = new AtomicLong(0);
        /** Current files processed */
        AtomicLong mFiles = new AtomicLong(0);
        /** Maximum files */
        AtomicLong mMaxFiles = new AtomicLong(0);

        // Completion information

        /** Whether patching was successful */
        AtomicBoolean mSuccessful = new AtomicBoolean(false);
        /** Error code if patching failed */
        AtomicInteger mErrorCode = new AtomicInteger(0);
        /** Path to the newly patched file */
        AtomicReference<String> mNewPath = new AtomicReference<>();

        public PatchFileTask(PatcherService service, int taskId) {
            super(service);
            mTaskId = taskId;
        }

        public void cancel() {
            // If the file was patching, then it should be considered cancelled
            mCancelled.set(true);

            synchronized (this) {
                if (mPatcher != null) {
                    mPatcher.cancelPatching();
                }
            }

            // If the file has not started patching yet, allow it to be patched later
            mState.compareAndSet(PatchFileState.PENDING, PatchFileState.QUEUED);
        }

        @Override
        public void run() {
            if (mExecuted) {
                throw new IllegalStateException("Task " + mTaskId + " has already been executed!");
            } else {
                mExecuted = true;
            }

            mState.set(PatchFileState.IN_PROGRESS);
            getService().onPatcherStarted(mTaskId);

            Log.d(TAG, "Android GUI version: " + BuildConfig.VERSION_NAME);
            Log.d(TAG, "libmbp version: " + PatcherUtils.sPC.getVersion());
            Log.d(TAG, "Patching file:");
            Log.d(TAG, "- Patcher ID: " + mPatcherId);
            Log.d(TAG, "- Path:       " + mPath);
            Log.d(TAG, "- Device:     " + mDevice.getId());
            Log.d(TAG, "- ROM ID:     " + mRomId);

            // Make sure patcher is extracted first
            PatcherUtils.initializePatcher(getService());

            // Create patcher instance
            synchronized (this) {
                mPatcher = PatcherUtils.sPC.createPatcher(mPatcherId);
            }
            FileInfo fileInfo = new FileInfo();
            try {
                fileInfo.setDevice(mDevice);
                fileInfo.setFilename(mPath);
                fileInfo.setRomId(mRomId);

                mPatcher.setFileInfo(fileInfo);

                boolean ret = mPatcher.patchFile(this);
                mSuccessful.set(ret);
                mErrorCode.set(mPatcher.getError());
                mNewPath.set(mPatcher.newFilePath());

                boolean cancelled = mCancelled.get();

                getService().onPatcherFinished(mTaskId, cancelled, ret, mPatcher.getError(),
                        mPatcher.newFilePath());

                // Set to complete if the task wasn't cancelled
                if (cancelled) {
                    mState.set(PatchFileState.CANCELLED);
                } else {
                    mState.set(PatchFileState.COMPLETED);
                }
            } finally {
                // Ensure we destroy allocated objects on the C++ side
                synchronized (this) {
                    PatcherUtils.sPC.destroyPatcher(mPatcher);
                    mPatcher = null;
                }
                fileInfo.destroy();

                // Save log
                LogUtils.dump("patch-file.log");
            }
        }

        @Override
        public void onProgressUpdated(long bytes, long maxBytes) {
            mBytes.set(bytes);
            mMaxBytes.set(maxBytes);
            getService().onPatcherUpdateProgress(mTaskId, bytes, maxBytes);
        }

        @Override
        public void onFilesUpdated(long files, long maxFiles) {
            mFiles.set(files);
            mMaxFiles.set(maxFiles);
            getService().onPatcherUpdateFilesProgress(mTaskId, files, maxFiles);
        }

        @Override
        public void onDetailsUpdated(String text) {
            mDetails.set(text);
            getService().onPatcherUpdateDetails(mTaskId, text);
        }
    }

    private static final class InitializePatcherTask extends BaseTask {
        public InitializePatcherTask(PatcherService service) {
            super(service);
        }

        @Override
        public void run() {
            PatcherUtils.initializePatcher(getService());
            getService().onPatcherInitialized();
        }
    }
}
