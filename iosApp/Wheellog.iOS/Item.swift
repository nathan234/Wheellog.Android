//
//  Item.swift
//  Wheellog.iOS
//
//  Created by nathan retta on 1/14/24.
//

import Foundation
import SwiftData

@Model
final class Item {
    var timestamp: Date
    
    init(timestamp: Date) {
        self.timestamp = timestamp
    }
}
