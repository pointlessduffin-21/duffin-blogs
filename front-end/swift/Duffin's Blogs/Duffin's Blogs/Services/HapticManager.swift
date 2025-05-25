//
//  HapticManager.swift
//  Duffin's Blogs
//
//  Created by Assistant on 25/5/2025.
//

import Foundation
import UIKit

struct HapticManager {
    static let shared = HapticManager()
    
    private init() {}
    
    func impact(_ style: UIImpactFeedbackGenerator.FeedbackStyle) {
        let impactGenerator = UIImpactFeedbackGenerator(style: style)
        impactGenerator.impactOccurred()
    }
    
    func notification(_ type: UINotificationFeedbackGenerator.FeedbackType) {
        let notificationGenerator = UINotificationFeedbackGenerator()
        notificationGenerator.notificationOccurred(type)
    }
    
    func selection() {
        let selectionGenerator = UISelectionFeedbackGenerator()
        selectionGenerator.selectionChanged()
    }
}
