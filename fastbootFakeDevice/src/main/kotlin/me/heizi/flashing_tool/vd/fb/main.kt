package me.heizi.flashing_tool.vd


fun main() {

}

const val fake =
"""
(bootloader) parallel-download-flash:yes
(bootloader) hw-revision:20001
(bootloader) eio_count:0
(bootloader) dm_count:0
(bootloader) lock_count:0
(bootloader) unlock_count:1
(bootloader) unlocked:yes
(bootloader) off-mode-charge:0
(bootloader) charger-screen-enabled:0
(bootloader) battery-soc-ok:yes
(bootloader) battery-voltage:3935
(bootloader) version-baseband:
(bootloader) version-bootloader:
(bootloader) erase-block-size: 0x1000
(bootloader) logical-block-size: 0x1000
(bootloader) variant:SDM UFS
(bootloader) partition-type:frp:raw
(bootloader) partition-size:frp: 0x200000
(bootloader) partition-type:fsc:raw
(bootloader) partition-size:fsc: 0x80000
(bootloader) partition-type:fsg:raw
(bootloader) partition-size:fsg: 0x200000
(bootloader) partition-type:modemst2:raw
(bootloader) partition-size:modemst2: 0x200000
(bootloader) partition-type:modemst1:raw
(bootloader) partition-size:modemst1: 0x200000
(bootloader) partition-type:ALIGN_TO_128K_2:raw
(bootloader) partition-size:ALIGN_TO_128K_2: 0x1A000
(bootloader) partition-type:logdump:raw
(bootloader) partition-size:logdump: 0x4000000
(bootloader) partition-type:sti:raw
(bootloader) partition-size:sti: 0x200000
(bootloader) partition-type:logfs:raw
(bootloader) partition-size:logfs: 0x800000
(bootloader) partition-type:toolsfv:raw
(bootloader) partition-size:toolsfv: 0x100000
(bootloader) partition-type:limits:raw
(bootloader) partition-size:limits: 0x1000
(bootloader) partition-type:spunvm:raw
(bootloader) partition-size:spunvm: 0x800000
(bootloader) partition-type:msadp:raw
(bootloader) partition-size:msadp: 0x80000
(bootloader) partition-type:apdp:raw
(bootloader) partition-size:apdp: 0x80000
(bootloader) partition-type:dip:raw
(bootloader) partition-size:dip: 0x100000
(bootloader) partition-type:devinfo:raw
(bootloader) partition-size:devinfo: 0x40000
(bootloader) partition-type:sec:raw
(bootloader) partition-size:sec: 0x40000
(bootloader) partition-type:sid_b:raw
(bootloader) partition-size:sid_b: 0x80000
(bootloader) partition-type:raw_resources_b:raw
(bootloader) partition-size:raw_resources_b: 0x800000
(bootloader) partition-type:storsec_b:raw
(bootloader) partition-size:storsec_b: 0x20000
(bootloader) partition-type:dtbo_b:raw
(bootloader) partition-size:dtbo_b: 0x800000
(bootloader) partition-type:vbmeta_b:raw
(bootloader) partition-size:vbmeta_b: 0x10000
(bootloader) partition-type:laf_b:raw
(bootloader) partition-size:laf_b: 0x3000000
(bootloader) partition-type:qupfw_b:raw
(bootloader) partition-size:qupfw_b: 0x20000
(bootloader) partition-type:devcfg_b:raw
(bootloader) partition-size:devcfg_b: 0x20000
(bootloader) partition-type:cmnlib64_b:raw
(bootloader) partition-size:cmnlib64_b: 0x80000
(bootloader) partition-type:cmnlib_b:raw
(bootloader) partition-size:cmnlib_b: 0x80000
(bootloader) partition-type:boot_b:raw
(bootloader) partition-size:boot_b: 0x4000000
(bootloader) partition-type:akmu_b:raw
(bootloader) partition-size:akmu_b: 0x80000
(bootloader) partition-type:keymaster_b:raw
(bootloader) partition-size:keymaster_b: 0x80000
(bootloader) partition-type:dsp_b:raw
(bootloader) partition-size:dsp_b: 0x2000000
(bootloader) partition-type:abl_b:raw
(bootloader) partition-size:abl_b: 0x100000
(bootloader) partition-type:mdtp_b:raw
(bootloader) partition-size:mdtp_b: 0x2000000
(bootloader) partition-type:mdtpsecapp_b:raw
(bootloader) partition-size:mdtpsecapp_b: 0x400000
(bootloader) partition-type:modem_b:raw
(bootloader) partition-size:modem_b: 0xB600000
(bootloader) partition-type:hyp_b:raw
(bootloader) partition-size:hyp_b: 0x80000
(bootloader) partition-type:tz_b:raw
(bootloader) partition-size:tz_b: 0x200000
(bootloader) partition-type:aop_b:raw
(bootloader) partition-size:aop_b: 0x80000
(bootloader) partition-type:sid_a:raw
(bootloader) partition-size:sid_a: 0x80000
(bootloader) partition-type:raw_resources_a:raw
(bootloader) partition-size:raw_resources_a: 0x800000
(bootloader) partition-type:storsec_a:raw
(bootloader) partition-size:storsec_a: 0x20000
(bootloader) partition-type:dtbo_a:raw
(bootloader) partition-size:dtbo_a: 0x800000
(bootloader) partition-type:vbmeta_a:raw
(bootloader) partition-size:vbmeta_a: 0x10000
(bootloader) partition-type:laf_a:raw
(bootloader) partition-size:laf_a: 0x3000000
(bootloader) partition-type:qupfw_a:raw
(bootloader) partition-size:qupfw_a: 0x20000
(bootloader) partition-type:devcfg_a:raw
(bootloader) partition-size:devcfg_a: 0x20000
(bootloader) partition-type:cmnlib64_a:raw
(bootloader) partition-size:cmnlib64_a: 0x80000
(bootloader) partition-type:cmnlib_a:raw
(bootloader) partition-size:cmnlib_a: 0x80000
(bootloader) partition-type:boot_a:raw
(bootloader) partition-size:boot_a: 0x4000000
(bootloader) partition-type:akmu_a:raw
(bootloader) partition-size:akmu_a: 0x80000
(bootloader) partition-type:keymaster_a:raw
(bootloader) partition-size:keymaster_a: 0x80000
(bootloader) partition-type:dsp_a:raw
(bootloader) partition-size:dsp_a: 0x2000000
(bootloader) partition-type:abl_a:raw
(bootloader) partition-size:abl_a: 0x100000
(bootloader) partition-type:mdtp_a:raw
(bootloader) partition-size:mdtp_a: 0x2000000
(bootloader) partition-type:mdtpsecapp_a:raw
(bootloader) partition-size:mdtpsecapp_a: 0x400000
(bootloader) partition-type:modem_a:raw
(bootloader) partition-size:modem_a: 0xB600000
(bootloader) partition-type:hyp_a:raw
(bootloader) partition-size:hyp_a: 0x80000
(bootloader) partition-type:tz_a:raw
(bootloader) partition-size:tz_a: 0x200000
(bootloader) partition-type:aop_a:raw
(bootloader) partition-size:aop_a: 0x80000
(bootloader) partition-type:ddr:raw
(bootloader) partition-size:ddr: 0x100000
(bootloader) partition-type:cdt:raw
(bootloader) partition-size:cdt: 0x20000
(bootloader) partition-type:ALIGN_TO_128K_1:raw
(bootloader) partition-size:ALIGN_TO_128K_1: 0x1A000
(bootloader) partition-type:xbl_config_b:raw
(bootloader) partition-size:xbl_config_b: 0x20000
(bootloader) partition-type:xbl_b:raw
(bootloader) partition-size:xbl_b: 0x380000
(bootloader) partition-type:xbl_config_a:raw
(bootloader) partition-size:xbl_config_a: 0x20000
(bootloader) partition-type:xbl_a:raw
(bootloader) partition-size:xbl_a: 0x380000
(bootloader) partition-type:grow:raw
(bootloader) partition-size:grow: 0x1000
(bootloader) partition-type:userdata:ext4
(bootloader) partition-size:userdata: 0xB86CF4000
(bootloader) partition-type:OP_b:raw
(bootloader) partition-size:OP_b: 0x2EE00000
(bootloader) partition-type:OP_a:raw
(bootloader) partition-size:OP_a: 0x2EE00000
(bootloader) partition-type:system_b:ext4
(bootloader) partition-size:system_b: 0x11B800000
(bootloader) partition-type:system_a:ext4
(bootloader) partition-size:system_a: 0x11B800000
(bootloader) partition-type:vendor_b:raw
(bootloader) partition-size:vendor_b: 0x3E800000
(bootloader) partition-type:vendor_a:raw
(bootloader) partition-size:vendor_a: 0x3E800000
(bootloader) partition-type:oem_b:raw
(bootloader) partition-size:oem_b: 0x800000
(bootloader) partition-type:oem_a:raw
(bootloader) partition-size:oem_a: 0x800000
(bootloader) partition-type:persdata:raw
(bootloader) partition-size:persdata: 0x4000000
(bootloader) partition-type:carrier:raw
(bootloader) partition-size:carrier: 0x2800000
(bootloader) partition-type:els:raw
(bootloader) partition-size:els: 0x1000000
(bootloader) partition-type:pstore:raw
(bootloader) partition-size:pstore: 0x200000
(bootloader) partition-type:srtc:raw
(bootloader) partition-size:srtc: 0x800000
(bootloader) partition-type:fota:raw
(bootloader) partition-size:fota: 0xA00000
(bootloader) partition-type:rct:raw
(bootloader) partition-size:rct: 0x80000
(bootloader) partition-type:eksst:raw
(bootloader) partition-size:eksst: 0x80000
(bootloader) partition-type:encrypt:raw
(bootloader) partition-size:encrypt: 0x80000
(bootloader) partition-type:power:raw
(bootloader) partition-size:power: 0x2C00000
(bootloader) partition-type:ftm:raw
(bootloader) partition-size:ftm: 0x2000000
(bootloader) partition-type:misc:raw
(bootloader) partition-size:misc: 0x100000
(bootloader) partition-type:persist:raw
(bootloader) partition-size:persist: 0x2000000
(bootloader) partition-type:ssd:raw
(bootloader) partition-size:ssd: 0x80000
(bootloader) partition-type:sns:raw
(bootloader) partition-size:sns: 0x600000
(bootloader) partition-type:drm:raw
(bootloader) partition-size:drm: 0xE00000
(bootloader) partition-type:mpt:raw
(bootloader) partition-size:mpt: 0x2000000
(bootloader) has-slot:modem:yes
(bootloader) has-slot:system:yes
(bootloader) current-slot:a
(bootloader) has-slot:boot:yes
(bootloader) slot-retry-count:b:7
(bootloader) slot-unbootable:b:no
(bootloader) slot-successful:b:no
(bootloader) slot-retry-count:a:6
(bootloader) slot-unbootable:a:no
(bootloader) slot-successful:a:yes
(bootloader) ufs-bootlun:1(a)
(bootloader) slot-count:2
(bootloader) secure:yes
(bootloader) serialno:LMG710ULM785d0fea
(bootloader) product:sdm845
(bootloader) max-download-size:805306368
(bootloader) kernel:uefi
"""