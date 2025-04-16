// swift-tools-version:5.3
import PackageDescription

let package = Package(
   name: "Network",
   platforms: [
     .iOS(.v14)
   ],
   products: [
      .library(name: "Network", targets: ["Network"])
   ],
   targets: [
      .binaryTarget(
         name: "Network",
         path: "./build/XCFrameworks/debug/Network.xcframework"
      )
   ]
)
