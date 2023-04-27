import Foundation
import Capacitor
import Foundation
import Photos

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(CapacitorGalleryPlugin)
public class CapacitorGalleryPlugin: CAPPlugin {
    static let DEFAULT_THUMBNAIL_WIDTH = 512
    static let DEFAULT_THUMBNAIL_HEIGHT = 512
    static let DEFAULT_THUMBNAIL_QUALITY = 95
    static let DEFAULT_QUANTITY = 25
    static let DEFAULT_OFFSET = 0

    lazy var imageManager = PHCachingImageManager()
    
    @objc func getGalleryItems(_ call: CAPPluginCall) {
        checkAuthorization(allowed: {
            self._getGalleryItems(call)
        }, notAllowed: {
            call.reject("Access is not allowed")
        })
    }
    
    func checkAuthorization(allowed: @escaping () -> Void, notAllowed: @escaping () -> Void) {
        let status = PHPhotoLibrary.authorizationStatus()
        if status == PHAuthorizationStatus.authorized {
            allowed()
        } else {
            PHPhotoLibrary.requestAuthorization({ (newStatus) in
                if newStatus == PHAuthorizationStatus.authorized {
                    allowed()
                } else {
                    notAllowed()
                }
            })
        }
    }
    
    func _getGalleryItems(_ call: CAPPluginCall) {
        var assets: [JSObject] = []

        let quantity = call.getInt("quantity", CapacitorGalleryPlugin.DEFAULT_QUANTITY)
        let offset = call.getInt("offset", CapacitorGalleryPlugin.DEFAULT_OFFSET)

        let options = PHFetchOptions()

        var sortDescriptors = [] as [NSSortDescriptor];
        sortDescriptors.append(NSSortDescriptor(key: "creationDate", ascending: false))
        
        options.sortDescriptors = sortDescriptors

        var fetchResult = PHAsset.fetchAssets(with: options)
        
        let thumbnailWidth = CapacitorGalleryPlugin.DEFAULT_THUMBNAIL_WIDTH
        let thumbnailHeight = CapacitorGalleryPlugin.DEFAULT_THUMBNAIL_HEIGHT
        let thumbnailSize = CGSize(width: thumbnailWidth, height: thumbnailHeight)
        let thumbnailQuality = CapacitorGalleryPlugin.DEFAULT_THUMBNAIL_QUALITY
        let requestOptions = PHImageRequestOptions()
        requestOptions.isNetworkAccessAllowed = true
        requestOptions.version = .current
        requestOptions.deliveryMode = .opportunistic
        requestOptions.isSynchronous = true
        
        if fetchResult.count == 0 {
            call.resolve([
                "count": 0,
                "results": [],
                "nextMaxOffset": 0,
                "nextMaxQuantity": 0
            ])
            return
        } else if offset >= fetchResult.count {
            call.resolve([
                "count": fetchResult.count,
                "results": assets,
                "nextOffset": min((offset + quantity), fetchResult.count),
                "nextMaxQuantity": 0
            ])
            return
        }
                
        for i in offset...min((offset + quantity), fetchResult.count) - 1 {
            self.imageManager.requestImage(for: fetchResult.object(at: i) as PHAsset, targetSize: thumbnailSize, contentMode: .aspectFill, options: requestOptions, resultHandler: { (fetchedImage, error) in
                guard let image = fetchedImage else {
                    return
                }
                var asset = fetchResult.object(at: i) as PHAsset
                var result = JSObject()
                
                result["creationDate"] = asset.creationDate ?? ""
                result["id"] = asset.localIdentifier
                result["base64Image"] = "data:image/jpeg;base64," + (image.jpegData(compressionQuality: CGFloat(thumbnailQuality) / 100.0)?.base64EncodedString())!
            
                assets.append(result)
            })
        }

        call.resolve([
            "count": fetchResult.count,
            "results": assets,
            "nextOffset": min((offset + quantity), fetchResult.count),
            "nextMaxQuantity": (offset + quantity) > fetchResult.count ? 0 : fetchResult.count - (offset + quantity)
        ])
    }
}
