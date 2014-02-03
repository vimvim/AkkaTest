package rtmp.protocol.v2.handshake

/**
 *
 */
object Constants {

  final val HANDSHAKE_SIZE: Int = 1536
  final val DIGEST_LENGTH: Int = 32
  final val KEY_LENGTH: Int = 128

  final val HANDSHAKE_SIZE_SERVER: Int = (HANDSHAKE_SIZE * 2) + 1

  final val GENUINE_FMS_KEY: Array[Byte] = Array(0x47.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x6e.asInstanceOf[Byte], 0x75.asInstanceOf[Byte], 0x69.asInstanceOf[Byte], 0x6e.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x20.asInstanceOf[Byte], 0x41.asInstanceOf[Byte], 0x64.asInstanceOf[Byte], 0x6f.asInstanceOf[Byte], 0x62.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x20.asInstanceOf[Byte], 0x46.asInstanceOf[Byte], 0x6c.asInstanceOf[Byte], 0x61.asInstanceOf[Byte], 0x73.asInstanceOf[Byte], 0x68.asInstanceOf[Byte], 0x20.asInstanceOf[Byte], 0x4d.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x64.asInstanceOf[Byte], 0x69.asInstanceOf[Byte], 0x61.asInstanceOf[Byte], 0x20.asInstanceOf[Byte], 0x53.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x72.asInstanceOf[Byte], 0x76.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x72.asInstanceOf[Byte], 0x20.asInstanceOf[Byte], 0x30.asInstanceOf[Byte], 0x30.asInstanceOf[Byte], 0x31.asInstanceOf[Byte], 0xf0.asInstanceOf[Byte], 0xee.asInstanceOf[Byte], 0xc2.asInstanceOf[Byte], 0x4a.asInstanceOf[Byte], 0x80.asInstanceOf[Byte], 0x68.asInstanceOf[Byte], 0xbe.asInstanceOf[Byte], 0xe8.asInstanceOf[Byte], 0x2e.asInstanceOf[Byte], 0x00.asInstanceOf[Byte], 0xd0.asInstanceOf[Byte], 0xd1.asInstanceOf[Byte], 0x02.asInstanceOf[Byte], 0x9e.asInstanceOf[Byte], 0x7e.asInstanceOf[Byte], 0x57.asInstanceOf[Byte], 0x6e.asInstanceOf[Byte], 0xec.asInstanceOf[Byte], 0x5d.asInstanceOf[Byte], 0x2d.asInstanceOf[Byte], 0x29.asInstanceOf[Byte], 0x80.asInstanceOf[Byte], 0x6f.asInstanceOf[Byte], 0xab.asInstanceOf[Byte], 0x93.asInstanceOf[Byte], 0xb8.asInstanceOf[Byte], 0xe6.asInstanceOf[Byte], 0x36.asInstanceOf[Byte], 0xcf.asInstanceOf[Byte], 0xeb.asInstanceOf[Byte], 0x31.asInstanceOf[Byte], 0xae.asInstanceOf[Byte])

  final val GENUINE_FP_KEY: Array[Byte] = Array(0x47.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x6E.asInstanceOf[Byte], 0x75.asInstanceOf[Byte], 0x69.asInstanceOf[Byte], 0x6E.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x20.asInstanceOf[Byte], 0x41.asInstanceOf[Byte], 0x64.asInstanceOf[Byte], 0x6F.asInstanceOf[Byte], 0x62.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x20.asInstanceOf[Byte], 0x46.asInstanceOf[Byte], 0x6C.asInstanceOf[Byte], 0x61.asInstanceOf[Byte], 0x73.asInstanceOf[Byte], 0x68.asInstanceOf[Byte], 0x20.asInstanceOf[Byte], 0x50.asInstanceOf[Byte], 0x6C.asInstanceOf[Byte], 0x61.asInstanceOf[Byte], 0x79.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x72.asInstanceOf[Byte], 0x20.asInstanceOf[Byte], 0x30.asInstanceOf[Byte], 0x30.asInstanceOf[Byte], 0x31.asInstanceOf[Byte], 0xF0.asInstanceOf[Byte], 0xEE.asInstanceOf[Byte], 0xC2.asInstanceOf[Byte], 0x4A.asInstanceOf[Byte], 0x80.asInstanceOf[Byte], 0x68.asInstanceOf[Byte], 0xBE.asInstanceOf[Byte], 0xE8.asInstanceOf[Byte], 0x2E.asInstanceOf[Byte], 0x00.asInstanceOf[Byte], 0xD0.asInstanceOf[Byte], 0xD1.asInstanceOf[Byte], 0x02.asInstanceOf[Byte], 0x9E.asInstanceOf[Byte], 0x7E.asInstanceOf[Byte], 0x57.asInstanceOf[Byte], 0x6E.asInstanceOf[Byte], 0xEC.asInstanceOf[Byte], 0x5D.asInstanceOf[Byte], 0x2D.asInstanceOf[Byte], 0x29.asInstanceOf[Byte], 0x80.asInstanceOf[Byte], 0x6F.asInstanceOf[Byte], 0xAB.asInstanceOf[Byte], 0x93.asInstanceOf[Byte], 0xB8.asInstanceOf[Byte], 0xE6.asInstanceOf[Byte], 0x36.asInstanceOf[Byte], 0xCF.asInstanceOf[Byte], 0xEB.asInstanceOf[Byte], 0x31.asInstanceOf[Byte], 0xAE.asInstanceOf[Byte])

}
