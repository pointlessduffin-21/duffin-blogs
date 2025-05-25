//
//  SkeletonView.swift
//  Duffin's Blogs
//
//  Created by Assistant on 25/5/2025.
//

import SwiftUI

struct SkeletonView: View {
    @State private var animateGradient = false
    
    var body: some View {
        Rectangle()
            .fill(
                LinearGradient(
                    colors: [Color(.systemGray5), Color(.systemGray6), Color(.systemGray5)],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .mask(
                LinearGradient(
                    colors: [.clear, .black, .clear],
                    startPoint: animateGradient ? .leading : .trailing,
                    endPoint: animateGradient ? .trailing : .leading
                )
            )
            .onAppear {
                withAnimation(.easeInOut(duration: 1.5).repeatForever(autoreverses: false)) {
                    animateGradient.toggle()
                }
            }
    }
}

struct BlogPostSkeletonView: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Hero image skeleton
            SkeletonView()
                .frame(height: 200)
                .cornerRadius(12)
            
            VStack(alignment: .leading, spacing: 8) {
                // Title skeleton
                SkeletonView()
                    .frame(height: 24)
                    .frame(maxWidth: .infinity)
                
                SkeletonView()
                    .frame(height: 24)
                    .frame(maxWidth: 0.7 * UIScreen.main.bounds.width)
                
                // Tags skeleton
                HStack {
                    SkeletonView()
                        .frame(width: 60, height: 20)
                        .cornerRadius(10)
                    
                    SkeletonView()
                        .frame(width: 80, height: 20)
                        .cornerRadius(10)
                    
                    Spacer()
                }
                
                // Content preview skeleton
                SkeletonView()
                    .frame(height: 16)
                    .frame(maxWidth: .infinity)
                
                SkeletonView()
                    .frame(height: 16)
                    .frame(maxWidth: 0.9 * UIScreen.main.bounds.width)
                
                SkeletonView()
                    .frame(height: 16)
                    .frame(maxWidth: 0.6 * UIScreen.main.bounds.width)
                
                // Author and date skeleton
                HStack {
                    SkeletonView()
                        .frame(width: 100, height: 14)
                    
                    Spacer()
                    
                    SkeletonView()
                        .frame(width: 80, height: 14)
                }
            }
            .padding(.horizontal)
        }
        .padding()
    }
}

#Preview {
    ScrollView {
        LazyVStack {
            ForEach(0..<3) { _ in
                BlogPostSkeletonView()
            }
        }
    }
}
