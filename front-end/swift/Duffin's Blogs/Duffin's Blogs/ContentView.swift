//
//  ContentView.swift
//  Duffin's Blogs
//
//  Created by Roel Abarca on 5/25/25.
//

import SwiftUI

struct ContentView: View {
    @StateObject private var blogService = BlogService.shared
    
    var body: some View {
        Group {
            if blogService.isAuthenticated {
                BlogListView()
            } else {
                LoginView()
            }
        }
        .onAppear {
            // Initialize the blog service to check authentication status
            _ = blogService
        }
    }
}

#Preview {
    ContentView()
}
