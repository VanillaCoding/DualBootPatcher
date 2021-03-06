// automatically generated by the FlatBuffers compiler, do not modify

#ifndef FLATBUFFERS_GENERATED_MBSETKERNEL_MBTOOL_DAEMON_V3_H_
#define FLATBUFFERS_GENERATED_MBSETKERNEL_MBTOOL_DAEMON_V3_H_

#include "flatbuffers/flatbuffers.h"

#include "file_chmod_generated.h"
#include "file_close_generated.h"
#include "file_open_generated.h"
#include "file_read_generated.h"
#include "file_seek_generated.h"
#include "file_selinux_get_label_generated.h"
#include "file_selinux_set_label_generated.h"
#include "file_stat_generated.h"
#include "file_write_generated.h"
#include "mb_get_booted_rom_id_generated.h"
#include "mb_get_installed_roms_generated.h"
#include "mb_get_version_generated.h"
#include "mb_switch_rom_generated.h"
#include "path_chmod_generated.h"
#include "path_copy_generated.h"
#include "path_get_directory_size_generated.h"
#include "path_selinux_get_label_generated.h"
#include "path_selinux_set_label_generated.h"

namespace mbtool {
namespace daemon {
namespace v3 {
struct FileChmodRequest;
struct FileChmodResponse;
}  // namespace v3
}  // namespace daemon
}  // namespace mbtool
namespace mbtool {
namespace daemon {
namespace v3 {
struct FileCloseRequest;
struct FileCloseResponse;
}  // namespace v3
}  // namespace daemon
}  // namespace mbtool
namespace mbtool {
namespace daemon {
namespace v3 {
struct FileOpenRequest;
struct FileOpenResponse;
}  // namespace v3
}  // namespace daemon
}  // namespace mbtool
namespace mbtool {
namespace daemon {
namespace v3 {
struct FileReadRequest;
struct FileReadResponse;
}  // namespace v3
}  // namespace daemon
}  // namespace mbtool
namespace mbtool {
namespace daemon {
namespace v3 {
struct FileSeekRequest;
struct FileSeekResponse;
}  // namespace v3
}  // namespace daemon
}  // namespace mbtool
namespace mbtool {
namespace daemon {
namespace v3 {
struct StructStat;
struct FileStatRequest;
struct FileStatResponse;
}  // namespace v3
}  // namespace daemon
}  // namespace mbtool
namespace mbtool {
namespace daemon {
namespace v3 {
struct FileWriteRequest;
struct FileWriteResponse;
}  // namespace v3
}  // namespace daemon
}  // namespace mbtool
namespace mbtool {
namespace daemon {
namespace v3 {
struct FileSELinuxGetLabelRequest;
struct FileSELinuxGetLabelResponse;
}  // namespace v3
}  // namespace daemon
}  // namespace mbtool
namespace mbtool {
namespace daemon {
namespace v3 {
struct FileSELinuxSetLabelRequest;
struct FileSELinuxSetLabelResponse;
}  // namespace v3
}  // namespace daemon
}  // namespace mbtool
namespace mbtool {
namespace daemon {
namespace v3 {
struct PathChmodRequest;
struct PathChmodResponse;
}  // namespace v3
}  // namespace daemon
}  // namespace mbtool
namespace mbtool {
namespace daemon {
namespace v3 {
struct PathCopyRequest;
struct PathCopyResponse;
}  // namespace v3
}  // namespace daemon
}  // namespace mbtool
namespace mbtool {
namespace daemon {
namespace v3 {
struct PathSELinuxGetLabelRequest;
struct PathSELinuxGetLabelResponse;
}  // namespace v3
}  // namespace daemon
}  // namespace mbtool
namespace mbtool {
namespace daemon {
namespace v3 {
struct PathSELinuxSetLabelRequest;
struct PathSELinuxSetLabelResponse;
}  // namespace v3
}  // namespace daemon
}  // namespace mbtool
namespace mbtool {
namespace daemon {
namespace v3 {
struct PathGetDirectorySizeRequest;
struct PathGetDirectorySizeResponse;
}  // namespace v3
}  // namespace daemon
}  // namespace mbtool
namespace mbtool {
namespace daemon {
namespace v3 {
struct MbGetVersionRequest;
struct MbGetVersionResponse;
}  // namespace v3
}  // namespace daemon
}  // namespace mbtool
namespace mbtool {
namespace daemon {
namespace v3 {
struct MbRom;
struct MbGetInstalledRomsRequest;
struct MbGetInstalledRomsResponse;
}  // namespace v3
}  // namespace daemon
}  // namespace mbtool
namespace mbtool {
namespace daemon {
namespace v3 {
struct MbGetBootedRomIdRequest;
struct MbGetBootedRomIdResponse;
}  // namespace v3
}  // namespace daemon
}  // namespace mbtool
namespace mbtool {
namespace daemon {
namespace v3 {
struct MbSwitchRomRequest;
struct MbSwitchRomResponse;
}  // namespace v3
}  // namespace daemon
}  // namespace mbtool

namespace mbtool {
namespace daemon {
namespace v3 {

struct MbSetKernelRequest;
struct MbSetKernelResponse;

struct MbSetKernelRequest FLATBUFFERS_FINAL_CLASS : private flatbuffers::Table {
  const flatbuffers::String *rom_id() const { return GetPointer<const flatbuffers::String *>(4); }
  const flatbuffers::String *boot_blockdev() const { return GetPointer<const flatbuffers::String *>(6); }
  bool Verify(flatbuffers::Verifier &verifier) const {
    return VerifyTableStart(verifier) &&
           VerifyField<flatbuffers::uoffset_t>(verifier, 4 /* rom_id */) &&
           verifier.Verify(rom_id()) &&
           VerifyField<flatbuffers::uoffset_t>(verifier, 6 /* boot_blockdev */) &&
           verifier.Verify(boot_blockdev()) &&
           verifier.EndTable();
  }
};

struct MbSetKernelRequestBuilder {
  flatbuffers::FlatBufferBuilder &fbb_;
  flatbuffers::uoffset_t start_;
  void add_rom_id(flatbuffers::Offset<flatbuffers::String> rom_id) { fbb_.AddOffset(4, rom_id); }
  void add_boot_blockdev(flatbuffers::Offset<flatbuffers::String> boot_blockdev) { fbb_.AddOffset(6, boot_blockdev); }
  MbSetKernelRequestBuilder(flatbuffers::FlatBufferBuilder &_fbb) : fbb_(_fbb) { start_ = fbb_.StartTable(); }
  MbSetKernelRequestBuilder &operator=(const MbSetKernelRequestBuilder &);
  flatbuffers::Offset<MbSetKernelRequest> Finish() {
    auto o = flatbuffers::Offset<MbSetKernelRequest>(fbb_.EndTable(start_, 2));
    return o;
  }
};

inline flatbuffers::Offset<MbSetKernelRequest> CreateMbSetKernelRequest(flatbuffers::FlatBufferBuilder &_fbb,
   flatbuffers::Offset<flatbuffers::String> rom_id = 0,
   flatbuffers::Offset<flatbuffers::String> boot_blockdev = 0) {
  MbSetKernelRequestBuilder builder_(_fbb);
  builder_.add_boot_blockdev(boot_blockdev);
  builder_.add_rom_id(rom_id);
  return builder_.Finish();
}

struct MbSetKernelResponse FLATBUFFERS_FINAL_CLASS : private flatbuffers::Table {
  uint8_t success() const { return GetField<uint8_t>(4, 0); }
  bool Verify(flatbuffers::Verifier &verifier) const {
    return VerifyTableStart(verifier) &&
           VerifyField<uint8_t>(verifier, 4 /* success */) &&
           verifier.EndTable();
  }
};

struct MbSetKernelResponseBuilder {
  flatbuffers::FlatBufferBuilder &fbb_;
  flatbuffers::uoffset_t start_;
  void add_success(uint8_t success) { fbb_.AddElement<uint8_t>(4, success, 0); }
  MbSetKernelResponseBuilder(flatbuffers::FlatBufferBuilder &_fbb) : fbb_(_fbb) { start_ = fbb_.StartTable(); }
  MbSetKernelResponseBuilder &operator=(const MbSetKernelResponseBuilder &);
  flatbuffers::Offset<MbSetKernelResponse> Finish() {
    auto o = flatbuffers::Offset<MbSetKernelResponse>(fbb_.EndTable(start_, 1));
    return o;
  }
};

inline flatbuffers::Offset<MbSetKernelResponse> CreateMbSetKernelResponse(flatbuffers::FlatBufferBuilder &_fbb,
   uint8_t success = 0) {
  MbSetKernelResponseBuilder builder_(_fbb);
  builder_.add_success(success);
  return builder_.Finish();
}

}  // namespace v3
}  // namespace daemon
}  // namespace mbtool

#endif  // FLATBUFFERS_GENERATED_MBSETKERNEL_MBTOOL_DAEMON_V3_H_
