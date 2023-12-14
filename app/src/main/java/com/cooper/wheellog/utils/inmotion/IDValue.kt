package com.cooper.wheellog.utils.inmotion

internal enum class IDValue(val value: Int) {
    NoOp(0),
    GetFastInfo(0x0F550113),
    GetSlowInfo(0x0F550114),
    RideMode(0x0F550115),
    RemoteControl(0x0F550116),
    Calibration(0x0F550119),
    PinCode(0x0F550307),
    Light(0x0F55010D),
    HandleButton(0x0F55012E),
    SpeakerVolume(0x0F55060A),
    PlaySound(0x0F550609),
    Alert(0x0F780101),
}
