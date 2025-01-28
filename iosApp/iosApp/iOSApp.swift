import SwiftUI
import UIKit
import KmpApp

@main
struct iOSApp: App {

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        KmpApp.MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea()
    }
}

#Preview {
    ContentView()
}

