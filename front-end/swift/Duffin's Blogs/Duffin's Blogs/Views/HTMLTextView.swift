import SwiftUI
import WebKit
import UIKit

struct HTMLTextView: UIViewRepresentable {
    let htmlContent: String
    @State private var contentHeight: CGFloat = 100
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.scrollView.isScrollEnabled = false
        webView.isOpaque = false
        webView.backgroundColor = UIColor.clear
        webView.navigationDelegate = context.coordinator
        return webView
    }
    
    class Coordinator: NSObject, WKNavigationDelegate {
        var parent: HTMLTextView
        
        init(_ parent: HTMLTextView) {
            self.parent = parent
        }
        
        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            webView.evaluateJavaScript("document.readyState", completionHandler: { (complete, error) in
                if complete != nil {
                    webView.evaluateJavaScript("document.body.scrollHeight", completionHandler: { (height, error) in
                        if let height = height as? CGFloat {
                            DispatchQueue.main.async {
                                self.parent.contentHeight = height
                            }
                        }
                    })
                }
            })
        }
    }
    
    func updateUIView(_ uiView: WKWebView, context: Context) {
        let htmlString = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                    font-size: 17px;
                    line-height: 1.5;
                    color: #000000;
                    background-color: transparent;
                    margin: 0;
                    padding: 0;
                    word-wrap: break-word;
                }
                
                @media (prefers-color-scheme: dark) {
                    body {
                        color: #ffffff;
                    }
                }
                
                h1, h2, h3, h4, h5, h6 {
                    font-weight: 600;
                    margin: 20px 0 12px 0;
                    line-height: 1.25;
                }
                
                h1 { font-size: 28px; }
                h2 { font-size: 24px; }
                h3 { font-size: 20px; }
                h4 { font-size: 18px; }
                h5 { font-size: 16px; }
                h6 { font-size: 14px; }
                
                p {
                    margin: 12px 0;
                }
                
                strong {
                    font-weight: 600;
                }
                
                em {
                    font-style: italic;
                }
                
                code {
                    font-family: 'SF Mono', Monaco, Consolas, monospace;
                    background-color: rgba(175, 184, 193, 0.2);
                    padding: 2px 4px;
                    border-radius: 3px;
                    font-size: 85%;
                }
                
                @media (prefers-color-scheme: dark) {
                    code {
                        background-color: rgba(110, 118, 129, 0.4);
                    }
                }
                
                blockquote {
                    border-left: 4px solid #007AFF;
                    margin: 16px 0;
                    padding: 8px 16px;
                    background-color: rgba(0, 122, 255, 0.1);
                    border-radius: 0 8px 8px 0;
                }
                
                ul, ol {
                    margin: 12px 0;
                    padding-left: 24px;
                }
                
                li {
                    margin: 4px 0;
                }
                
                img {
                    max-width: 100%;
                    height: auto;
                    border-radius: 8px;
                    margin: 12px 0;
                }
                
                a {
                    color: #007AFF;
                    text-decoration: none;
                }
                
                a:hover {
                    text-decoration: underline;
                }
                
                @media (prefers-color-scheme: dark) {
                    a {
                        color: #0A84FF;
                    }
                }
                
                .embed-responsive {
                    position: relative;
                    width: 100%;
                    padding-bottom: 56.25%; /* 16:9 aspect ratio */
                    height: 0;
                    margin: 16px 0;
                }
                
                .embed-responsive iframe {
                    position: absolute;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    border: 0;
                    border-radius: 8px;
                }
                
                del {
                    text-decoration: line-through;
                    opacity: 0.7;
                }
            </style>
        </head>
        <body>
            \(htmlContent)
        </body>
        </html>
        """
        
        uiView.loadHTMLString(htmlString, baseURL: nil)
    }
}

#Preview {
    HTMLTextView(htmlContent: """
        <p>This is a test with <strong>bold text</strong> and <em>italic text</em>.</p>
        <h2>A Header</h2>
        <p>Some more content with <code>inline code</code>.</p>
        <blockquote>This is a quote</blockquote>
        <ul>
            <li>List item 1</li>
            <li>List item 2</li>
        </ul>
    """)
    .frame(height: 300)
}
