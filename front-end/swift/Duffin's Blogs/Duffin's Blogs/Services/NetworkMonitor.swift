//
//  NetworkMonitor.swift
//  Duffin's Blogs
//
//  Created by Assistant on 25/5/2025.
//

import Foundation
import Network
import SwiftUI

@MainActor
class NetworkMonitor: ObservableObject {
    private let networkMonitor = NWPathMonitor()
    private let workerQueue = DispatchQueue(label: "NetworkMonitor")
    
    @Published var isConnected = true
    @Published var connectionType: NWInterface.InterfaceType?
    
    init() {
        startMonitoring()
    }
    
    func startMonitoring() {
        networkMonitor.pathUpdateHandler = { [weak self] path in
            DispatchQueue.main.async {
                self?.isConnected = path.status == .satisfied
                self?.connectionType = path.availableInterfaces.first?.type
            }
        }
        networkMonitor.start(queue: workerQueue)
    }
    
    deinit {
        networkMonitor.cancel()
    }
}
