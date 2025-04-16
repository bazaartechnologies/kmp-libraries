// swift-tools-version:5.3
import PackageDescription

let package = Package(
    name: "KmpAppCommon",
    platforms: [
        .iOS(.v14),
    ],
    products: [
        .library(name: "KmpAppCommon", targets: ["KmpAppCommon"])
    ],
    targets: [
        .binaryTarget(
            name: "KmpAppCommon",
            path: "./build/XCFrameworks/debug/KmpAppCommon.xcframework"
        )
    ]
)
