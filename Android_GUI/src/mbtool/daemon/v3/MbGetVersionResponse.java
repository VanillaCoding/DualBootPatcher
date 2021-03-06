// automatically generated, do not modify

package mbtool.daemon.v3;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class MbGetVersionResponse extends Table {
  public static MbGetVersionResponse getRootAsMbGetVersionResponse(ByteBuffer _bb) { return getRootAsMbGetVersionResponse(_bb, new MbGetVersionResponse()); }
  public static MbGetVersionResponse getRootAsMbGetVersionResponse(ByteBuffer _bb, MbGetVersionResponse obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public MbGetVersionResponse __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public String version() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer versionAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }

  public static int createMbGetVersionResponse(FlatBufferBuilder builder,
      int version) {
    builder.startObject(1);
    MbGetVersionResponse.addVersion(builder, version);
    return MbGetVersionResponse.endMbGetVersionResponse(builder);
  }

  public static void startMbGetVersionResponse(FlatBufferBuilder builder) { builder.startObject(1); }
  public static void addVersion(FlatBufferBuilder builder, int versionOffset) { builder.addOffset(0, versionOffset, 0); }
  public static int endMbGetVersionResponse(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

