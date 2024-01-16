//
//  WheelLog.swift
//  Wheellog.iOS
//
//  Created by nathan retta on 1/14/24.
//

import Foundation
class WheelLog: ObservableObject {
    static let shared = WheelLog()
    // TODO

    @Published var appConfig: AppConfig
//    @Published var notifications: NotificationUtil
//    @Published var volumeKeyController: VolumeKeyController

    private init() {
        self.appConfig = AppConfig()
//        self.notifications = NotificationUtil()
//        self.volumeKeyController = VolumeKeyController()
        // Initialize your properties
    }

    // Your shared methods
//    func cResolver() -> ContentResolver {
//        // Implement ContentResolver functionality
//    }
}
