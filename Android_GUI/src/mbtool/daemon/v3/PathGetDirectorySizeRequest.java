// automatically generated, do not modify

package mbtool.daemon.v3;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class PathGetDirectorySizeRequest extends Table {
  public static PathGetDirectorySizeRequest getRootAsPathGetDirectorySizeRequest(ByteBuffer _bb) { return getRootAsPathGetDirectorySizeRequest(_bb, new PathGetDirectorySizeRequest()); }
  public static PathGetDirectorySizeRequest getRootAsPathGetDirectorySizeRequest(ByteBuffer _bb, PathGetDirectorySizeRequest obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public PathGetDirectorySizeRequest __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public String path() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer pathAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public String exclusions(int j) { int o = __offset(6); return o != 0 ? __string(__vector(o) + j * 4) : null; }
  public int exclusionsLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }

  public static int createPathGetDirectorySizeRequest(FlatBufferBuilder builder,
      int path,
      int exclusions) {
    builder.startObject(2);
    PathGetDirectorySizeRequest.addExclusions(builder, exclusions);
    PathGetDirectorySizeRequest.addPath(builder, path);
    return PathGetDirectorySizeRequest.endPathGetDirectorySizeRequest(builder);
  }

  public static void startPathGetDirectorySizeRequest(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addPath(FlatBufferBuilder builder, int pathOffset) { builder.addOffset(0, pathOffset, 0); }
  public static void addExclusions(FlatBufferBuilder builder, int exclusionsOffset) { builder.addOffset(1, exclusionsOffset, 0); }
  public static int createExclusionsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startExclusionsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endPathGetDirectorySizeRequest(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

