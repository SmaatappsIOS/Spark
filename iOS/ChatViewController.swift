//
//  ChatViewController.swift
//  Spark
//
//  Created by Bharathi on 18/11/16.
//  Copyright © 2016 Smaat. All rights reserved.
//

import UIKit
import CoreData

import JSQMessagesViewController

struct Conversation {
    
    let firstName: String?
    
    let lastName: String?
    
    let preferredName: String?
    
    let smsNumber: String
    
    let id: String?
    
    let latestMessage: String?
    
    let isRead: Bool
    
}

extension Date {
    func toUTCString() -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        dateFormatter.timeZone = NSTimeZone(abbreviation: "UTC") as TimeZone!
        return dateFormatter.string(from: self)
    }
    func toStringWithFormat(format: String) -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = format
        return dateFormatter.string(from: self)
    }
    
}


extension String {
    
    func toLocatStringWithFormat(format: String = "yyyy-MM-dd HH:mm:ss") -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = format
        dateFormatter.timeZone = NSTimeZone(abbreviation: "UTC") as TimeZone!
        let convertedDate = dateFormatter.date(from: self)
        dateFormatter.timeZone = NSTimeZone.local
        
        return dateFormatter.string(from: convertedDate!)
    }
    func toDate() -> Date {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"

        dateFormatter.timeZone = NSTimeZone(abbreviation: "UTC") as TimeZone!
        let convertedDate = dateFormatter.date(from: self)

        dateFormatter.timeZone = NSTimeZone.local

        let convertedString = dateFormatter.string(from: convertedDate!)

        return dateFormatter.date(from: convertedString)!
    }
    
    func toLocalDateDate() -> Date {
        let dateFormatter = DateFormatter()
        dateFormatter.timeZone = NSTimeZone.local
//        dateFormatter.timeZone = NSTimeZone(abbreviation: "UTC") as TimeZone!
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        return dateFormatter.date(from: self)!
    }

}

public enum Setting: String{
    
    case removeBubbleTails = "Remove message bubble tails"
    
    case removeSenderDisplayName = "Remove sender Display Name"
    
    case removeAvatar = "Remove Avatars"
    
}



class ChatViewController: JSQMessagesViewController, ServerAPIDelegate  , UITextFieldDelegate  {
    
    
    
    var isUserDisconnected = false
    var isBackButtonTapped = false
    var isValueLoadedFromDB = false
    
    ///
    var messages = [JSQMessage]()
    
    let defaults = UserDefaults.standard
    
    var conversation: Conversation?
    
    var incomingBubble: JSQMessagesBubbleImage!
    
    var outgoingBubble: JSQMessagesBubbleImage!
    
    fileprivate var displayName: String!
    ///
    
    var distanceSelected = ""
    var subjectSelected = ""
    var isDistanceSelected = "0"
    
    var friendId = ""
    var friendName = ""
    var chatArray = [ChatModel]()
    
    var connectAPiCallCount = 0
    
    var maxChatId = "0"
    
    let appdelegate = UIApplication.shared.delegate as! AppDelegate

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        callConnectAPI()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
     
        setupBackButton()
        
        self.title = "Searching For User..."
        
        // Navigation bar set ups
        self.navigationController?.navigationBar.barTintColor = UIColorFromRGB(color: HeaderBGColor, alpha: 1)
      
        self.navigationController?.navigationBar.titleTextAttributes  = [NSForegroundColorAttributeName :  UIColorFromRGB(color: ButtonBgBlueCOlor
            , alpha: 1), NSFontAttributeName: GRAPHICS.FONT_BOLD(s: 14)!]

        if defaults.bool(forKey: Setting.removeBubbleTails.rawValue) {
            
            // Make taillessBubbles
            
            incomingBubble = JSQMessagesBubbleImageFactory(bubble: UIImage.jsq_bubbleCompactTailless(), capInsets: UIEdgeInsets.zero, layoutDirection: UIApplication.shared.userInterfaceLayoutDirection).incomingMessagesBubbleImage(with: UIColorFromRGB(color: ButtonBgBlueCOlor, alpha: 1))
            
            outgoingBubble = JSQMessagesBubbleImageFactory(bubble: UIImage.jsq_bubbleCompactTailless(), capInsets: UIEdgeInsets.zero, layoutDirection: UIApplication.shared.userInterfaceLayoutDirection).outgoingMessagesBubbleImage(with: UIColor.lightGray)
            
        }
            
        else {
            
            // Bubbles with tails
            
            incomingBubble = JSQMessagesBubbleImageFactory().incomingMessagesBubbleImage(with: UIColorFromRGB(color: ButtonBgBlueCOlor, alpha: 1))
            
            outgoingBubble = JSQMessagesBubbleImageFactory().outgoingMessagesBubbleImage(with: UIColor.lightGray)
            
        }
        
        if defaults.bool(forKey: Setting.removeAvatar.rawValue) {
            
            collectionView?.collectionViewLayout.incomingAvatarViewSize = .zero
            
            collectionView?.collectionViewLayout.outgoingAvatarViewSize = .zero
        } else {
            collectionView?.collectionViewLayout.incomingAvatarViewSize = CGSize(width: kJSQMessagesCollectionViewAvatarSizeDefault, height:kJSQMessagesCollectionViewAvatarSizeDefault )
            
            collectionView?.collectionViewLayout.outgoingAvatarViewSize = CGSize(width: kJSQMessagesCollectionViewAvatarSizeDefault, height:kJSQMessagesCollectionViewAvatarSizeDefault )
        }
        
        inputToolbar.isUserInteractionEnabled = false
        
        collectionView?.collectionViewLayout.springinessEnabled = false
        automaticallyScrollsToMostRecentMessage = true
        self.collectionView?.reloadData()
        
        self.collectionView?.layoutIfNeeded()
        setRightActivitybar()
    }
    
    func setupBackButton() {
        let backButton = UIBarButtonItem(image: UIImage(named : "Back_button.png"), style: .plain, target: self, action: #selector(backButtonTapped))
        //UIBarButtonItem(title: "Back", style: UIBarButtonItemStyle.plain, target: self, action: #selector(backButtonTapped))
        
        navigationItem.leftBarButtonItem = backButton
    }
    
    
    func setRightActivitybar() {
        let activityIndicator = UIActivityIndicatorView(frame: CGRect(x: 0, y: 0, width: 20, height: 20))
        activityIndicator.activityIndicatorViewStyle = .white
        let barButton = UIBarButtonItem(customView: activityIndicator)
        navigationItem.rightBarButtonItem = barButton
        activityIndicator.startAnimating()
    }
    
    func backButtonTapped() {
    
        self.view .endEditing(true)
        if ServerInterface.sharedInstance.isNetAvailable() {
        self.inputToolbar.contentView?.textView?.resignFirstResponder()
        isBackButtonTapped = true
        callDisconnectAPI()
        }
        else {
            showValidationAlert(message: NO_INTERNET_AVAILABLE,presentVc: self){
            }

        }
    }
    
    // MARK: JSQMessagesViewController method overrides
    
    override func didPressSend(_ button: UIButton, withMessageText text: String, senderId: String, senderDisplayName: String, date: Date) {
        
        /**
         
         *  Sending a message. Your implementation of this method should do *at least* the following:
         
         *
         
         *  1. Play sound (optional)
         
         *  2. Add new id<JSQMessageData> object to your data source
         
         *  3. Call `finishSendingMessage`
         
         */
        
        
        if ServerInterface.sharedInstance.isNetAvailable() {
        if friendId.characters.count > 0 && !isUserDisconnected  {
            let message = JSQMessage(senderId: senderId, senderDisplayName: "", date: date, text: text)
            self.messages.append(message)
            
            self.finishSendingMessage(animated: true)
            // call APi
            self.view.endEditing(true)
            
            var goodStr = text
//            if let data  = text.data(using: .nonLossyASCII) {
//             goodStr =  String(data: data, encoding: .utf8)!
//            }

            goodStr = text.addingPercentEncoding( withAllowedCharacters: .urlQueryAllowed)!

//            let encodetext =  text.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed)
            
            let params = ["user_id" : getUserId(), "friend_id" : friendId, "subject": subjectSelected, "message" : goodStr]
            RequestGenerator.sharedInstance.requestData(apiName: sendChatAPi, params: params as [String : AnyObject], delegate: self)
            
            // Save into local DB
            let chatEnt = ChatModel()
            chatEnt.chatId = "0"
            chatEnt.friendId = friendId
            chatEnt.userId  = getUserId()
            chatEnt.message = text
            chatEnt.datetime = date.toUTCString()
//            saveChatIntoDB(chatEnt: chatEnt)
        }
        else { // not conected yet
        }
        
        }
        else {
            // no internet
//            showValidationAlert(message: NO_INTERNET_AVAILABLE, presentVc: self, completion: { 
//                
//            })
            
            self.view.makeToast(message: "Internet not available")
        }
    }
    
    override func didPressAccessoryButton(_ sender: UIButton) {
        
        self.inputToolbar.contentView!.textView!.resignFirstResponder()
        
        let sheet = UIAlertController(title: "Media messages", message: nil, preferredStyle: .actionSheet)
        
        //        let photoAction = UIAlertAction(title: "Send photo", style: .default) { (action) in
        //
        //            /**
        //
        //             *  Create fake photo
        //
        //             */
        //
        //            let photoItem = JSQPhotoMediaItem(image: UIImage(named: "goldengate"))
        //
        //            self.addMedia(photoItem)
        //
        //        }
        
        
        
        //        let locationAction = UIAlertAction(title: "Send location", style: .default) { (action) in
        //
        //            /**
        //
        //             *  Add fake location
        //
        //             */
        //
        //            let locationItem = self.buildLocationItem()
        //
        //
        //
        //            self.addMedia(locationItem)
        //
        //        }
        //
        
        
        //        let videoAction = UIAlertAction(title: "Send video", style: .default) { (action) in
        //
        //            /**
        //
        //             *  Add fake video
        //
        //             */
        //
        //            let videoItem = self.buildVideoItem()
        //
        //
        //
        //            self.addMedia(videoItem)
        //
        //        }
        
        
        
        //        let audioAction = UIAlertAction(title: "Send audio", style: .default) { (action) in
        //
        //            /**
        //
        //             *  Add fake audio
        //
        //             */
        //
        //            let audioItem = self.buildAudioItem()
        //
        //
        //
        //            self.addMedia(audioItem)
        
        //        }
        
        
        
        let cancelAction = UIAlertAction(title: "Cancel", style: .cancel, handler: nil)
        
        
        
        //        sheet.addAction(photoAction)
        //
        //        sheet.addAction(locationAction)
        //
        //        sheet.addAction(videoAction)
        //
        //        sheet.addAction(audioAction)
        //
        sheet.addAction(cancelAction)
        //
        
        
        self.present(sheet, animated: true, completion: nil)
        
    }
    
    
    
    func buildVideoItem() -> JSQVideoMediaItem {
        
        let videoURL = URL(fileURLWithPath: "file://")
        
        
        
        let videoItem = JSQVideoMediaItem(fileURL: videoURL, isReadyToPlay: true)
        
        
        
        return videoItem
        
    }
    
    
    
    //    func buildAudioItem() -> JSQAudioMediaItem {
    //
    //        let sample = Bundle.main.path(forResource: "jsq_messages_sample", ofType: "m4a")
    //
    //        let audioData = Data(
    //            NSData(contentsOf: URL(fileURLWithPath: sample!))
    //            let audioItem = JSQAudioMediaItem(data: audioData as! Data)
    //            return audioItem
    //
    //            }
    //
    
    
    func buildLocationItem() -> JSQLocationMediaItem {
        
        let ferryBuildingInSF = CLLocation(latitude: 37.795313, longitude: -122.393757)
        
        
        
        let locationItem = JSQLocationMediaItem()
        
        locationItem.setLocation(ferryBuildingInSF) {
            
            self.collectionView!.reloadData()
            
        }
        
        
        
        return locationItem
        
    }
    
    
    
    func addMedia(_ media:JSQMediaItem) {
        
        let message = JSQMessage(senderId: self.senderId(), displayName: self.senderDisplayName(), media: media)
        
        self.messages.append(message)
        
        
        
        //Optional: play sent sound
        
        
        
        self.finishSendingMessage(animated: true)
        
    }
    
    
    
    
    
    //MARK: JSQMessages CollectionView DataSource
    
    
    
    override func senderId() -> String {
        
        return getUserId()
    }
    
    
    
    override func senderDisplayName() -> String {
        
        return getName(.Wozniak)
        
    }
    
    
    
    override func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        
        return messages.count
        
    }
    
    
    
    override func collectionView(_ collectionView: JSQMessagesCollectionView, messageDataForItemAt indexPath: IndexPath) -> JSQMessageData {
        
        return messages[indexPath.item]
        
    }
    
    
    
    override func collectionView(_ collectionView: JSQMessagesCollectionView, messageBubbleImageDataForItemAt indexPath: IndexPath) -> JSQMessageBubbleImageDataSource {
        
        
        
        return messages[indexPath.item].senderId == self.senderId() ? outgoingBubble : incomingBubble
        
    }
    
    
    
    override func collectionView(_ collectionView: JSQMessagesCollectionView, avatarImageDataForItemAt indexPath: IndexPath) -> JSQMessageAvatarImageDataSource? {
        
        let message = messages[indexPath.item]
        
        return getAvatar(message.senderId)
        
    }
    
    
    
    override func collectionView(_ collectionView: JSQMessagesCollectionView, attributedTextForCellTopLabelAt indexPath: IndexPath) -> NSAttributedString? {
        
        /**
         
         *  This logic should be consistent with what you return from `heightForCellTopLabelAtIndexPath:`
         
         *  The other label text delegate methods should follow a similar pattern.
         
         *
         
         *  Show a timestamp for every 3rd message
         
         */
        
        //        if (indexPath.item % 3 == 0) {
        //
        //            let message = self.messages[indexPath.item]
        ////
        //            return JSQMessagesTimestampFormatter.shared().attributedTimestamp(for: message.date)
        //
        //        }
        
        
        
        return nil
        
    }
    
    
    
    override func collectionView(_ collectionView: JSQMessagesCollectionView, attributedTextForMessageBubbleTopLabelAt indexPath: IndexPath) -> NSAttributedString? {
        
        let message = messages[indexPath.item]
        
        
        
        // Displaying names above messages
        
        //Mark: Removing Sender Display Name
        
        /**
         
         *  Example on showing or removing senderDisplayName based on user settings.
         
         *  This logic should be consistent with what you return from `heightForCellTopLabelAtIndexPath:`
         
         */
        
        
        return NSAttributedString(string:message.date.toStringWithFormat(format: "hh:mm a"))
        //        return JSQMessagesTimestampFormatter.shared().attributedTimestamp(for: message.date)
        
        
        //        if defaults.bool(forKey: Setting.removeSenderDisplayName.rawValue) {
        //
        //            return nil
        //
        //        }
        //
        //
        //
        //        if message.senderId == self.senderId() {
        //
        //            return nil
        //
        //        }
        //
        //
        
        //        return NSAttributedString(string: message.senderDisplayName)
        
    }
    
    
    
    override func collectionView(_ collectionView: JSQMessagesCollectionView, layout collectionViewLayout: JSQMessagesCollectionViewFlowLayout, heightForCellTopLabelAt indexPath: IndexPath) -> CGFloat {
        
        /**
         
         *  Each label in a cell has a `height` delegate method that corresponds to its text dataSource method
         
         */
        
        
        
        /**
         
         *  This logic should be consistent with what you return from `attributedTextForCellTopLabelAtIndexPath:`
         
         *  The other label height delegate methods should follow similarly
         
         *
         
         *  Show a timestamp for every 3rd message
         
         */
        
        //        if indexPath.item % 3 == 0 {
        
        //            return kJSQMessagesCollectionViewCellLabelHeightDefault
        
        //        }
        //
        //
        //
        return 0.0
        
    }
    
    
    
    override func collectionView(_ collectionView: JSQMessagesCollectionView, layout collectionViewLayout: JSQMessagesCollectionViewFlowLayout, heightForMessageBubbleTopLabelAt indexPath: IndexPath) -> CGFloat {
        
        
        
        /**
         
         *  Example on showing or removing senderDisplayName based on user settings.
         
         *  This logic should be consistent with what you return from `attributedTextForCellTopLabelAtIndexPath:`
         
         */
        
        //        if defaults.bool(forKey: Setting.removeSenderDisplayName.rawValue) {
        //
        //            return 0.0
        //
        //        }
        
        
        
        /**
         
         *  iOS7-style sender name labels
         
         */
        
        //        let currentMessage = self.messages[indexPath.item]
        
        
        
        //        if currentMessage.senderId == self.senderId() {
        //
        //            return 0.0
        //
        //        }
        //
        
        
        //        if indexPath.item - 1 > 0 {
        //
        //            let previousMessage = self.messages[indexPath.item - 1]
        //
        //            if previousMessage.senderId == currentMessage.senderId {
        //
        //                return 0.0
        //
        //            }
        //
        //        }
        
        
        
        return kJSQMessagesCollectionViewCellLabelHeightDefault;
        
    }
    
    
    
    func receiveMessagePressed(_ sender: UIBarButtonItem) {
        
        /**
         
         *  DEMO ONLY
         
         *
         
         *  The following is simply to simulate received messages for the demo.
         
         *  Do not actually do this.
         
         */
        
        
        
        /**
         
         *  Show the typing indicator to be shown
         
         */
        
        self.showTypingIndicator = !self.showTypingIndicator
        
        
        
        /**
         
         *  Scroll to actually view the indicator
         
         */
        
        self.scrollToBottom(animated: true)
        
        
        
        /**
         
         *  Copy last sent message, this will be the new "received" message
         
         */
        
        var copyMessage = self.messages.last?.copy()
        
        
        
        if (copyMessage == nil) {
            
            copyMessage = JSQMessage(senderId: AvatarIdJobs, displayName: getName(User.Jobs), text: "First received!")
            
        }
        
        
        
        var newMessage:JSQMessage!
        
        var newMediaData:JSQMessageMediaData!
        
        var newMediaAttachmentCopy:AnyObject?
        
        
        
        if (copyMessage! as AnyObject).isMediaMessage() {
            
            /**
             
             *  Last message was a media message
             
             */
            
            let copyMediaData = (copyMessage! as AnyObject).media
            
            
            
            switch copyMediaData {
                
            case is JSQPhotoMediaItem:
                
                let photoItemCopy = (copyMediaData as! JSQPhotoMediaItem).copy() as! JSQPhotoMediaItem
                
                photoItemCopy.appliesMediaViewMaskAsOutgoing = false
                
                
                
                newMediaAttachmentCopy = UIImage(cgImage: photoItemCopy.image!.cgImage!)
                
                
                
                /**
                 
                 *  Set image to nil to simulate "downloading" the image
                 
                 *  and show the placeholder view5017
                 
                 */
                
                photoItemCopy.image = nil;
                
                
                
                newMediaData = photoItemCopy
                
            case is JSQLocationMediaItem:
                
                let locationItemCopy = (copyMediaData as! JSQLocationMediaItem).copy() as! JSQLocationMediaItem
                
                locationItemCopy.appliesMediaViewMaskAsOutgoing = false
                
                newMediaAttachmentCopy = locationItemCopy.location!.copy() as AnyObject?
                
                
                
                /**
                 
                 *  Set location to nil to simulate "downloading" the location data
                 
                 */
                
                locationItemCopy.location = nil;
                
                
                
                newMediaData = locationItemCopy;
                
            case is JSQVideoMediaItem:
                
                let videoItemCopy = (copyMediaData as! JSQVideoMediaItem).copy() as! JSQVideoMediaItem
                
                videoItemCopy.appliesMediaViewMaskAsOutgoing = false
                
                newMediaAttachmentCopy = (videoItemCopy.fileURL! as NSURL).copy() as AnyObject?
                
                
                
                /**
                 
                 *  Reset video item to simulate "downloading" the video
                 
                 */
                
                videoItemCopy.fileURL = nil;
                
                videoItemCopy.isReadyToPlay = false;
                
                
                
                newMediaData = videoItemCopy;
                
            case is JSQAudioMediaItem:
                
                let audioItemCopy = (copyMediaData as! JSQAudioMediaItem).copy() as! JSQAudioMediaItem
                
                audioItemCopy.appliesMediaViewMaskAsOutgoing = false
                
                newMediaAttachmentCopy = (audioItemCopy.audioData! as NSData).copy() as AnyObject?
                
                
                
                /**
                 
                 *  Reset audio item to simulate "downloading" the audio
                 
                 */
                
                audioItemCopy.audioData = nil;
                
                
                
                newMediaData = audioItemCopy;
                
            default:
                
                assertionFailure("Error: This Media type was not recognised")
                
            }
            
            
            
            newMessage = JSQMessage(senderId: AvatarIdJobs, displayName: getName(User.Jobs), media: newMediaData)
            
        }
            
        else {
            
            /**
             
             *  Last message was a text message
             
             */
            
            
            
            newMessage = JSQMessage(senderId: AvatarIdJobs, displayName: getName(User.Jobs), text: (copyMessage! as AnyObject).text)
            
        }
        
        
        
        /**
         
         *  Upon receiving a message, you should:
         
         *
         
         *  1. Play sound (optional)
         
         *  2. Add new JSQMessageData object to your data source
         
         *  3. Call `finishReceivingMessage`
         
         */
        
        self.messages.append(newMessage)
        
        self.finishReceivingMessage(animated: true)
        
        
        
        if newMessage.isMediaMessage {
            
            /**
             
             *  Simulate "downloading" media
             
             */
            
            DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + Double(Int64(1 * Double(NSEC_PER_SEC))) / Double(NSEC_PER_SEC)) {
                
                /**
                 
                 *  Media is "finished downloading", re-display visible cells
                 
                 *
                 
                 *  If media cell is not visible, the next time it is dequeued the view controller will display its new attachment data
                 
                 *
                 
                 *  Reload the specific item, or simply call `reloadData`
                 
                 */
                
                
                
                switch newMediaData {
                    
                case is JSQPhotoMediaItem:
                    
                    (newMediaData as! JSQPhotoMediaItem).image = newMediaAttachmentCopy as? UIImage
                    
                    self.collectionView!.reloadData()
                    
                    //                case is JSQLocationMediaItem:
                    //
                    //                    (newMediaData as! JSQLocationMediaItem).setLocation(newMediaAttachmentCopy as? CLLocation, withCompletionHandler: {
                    //
                    //                        self.collectionView!.reloadData()
                    //
                    //                    })
                    //
                    //                case is JSQVideoMediaItem:
                    //
                    //                    (newMediaData as! JSQVideoMediaItem).fileURL = newMediaAttachmentCopy as? URL
                    //
                    //                    (newMediaData as! JSQVideoMediaItem).isReadyToPlay = true
                    //
                    //                    self.collectionView!.reloadData()
                    
                    //                case is JSQAudioMediaItem:
                    //
                    //                    (newMediaData as! JSQAudioMediaItem).audioData = newMediaAttachmentCopy as? Data
                    //
                    //                    self.collectionView!.reloadData()
                    
                default:
                    assertionFailure("Error: This Media type was not recognised")
                    
                }
                
            }
            
        }
        
    }
    
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }
    
   
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
   
    
    //MARK:- API Handlers
    func callConnectAPI() {
        
        if !isBackButtonTapped {
            
            print("calling API")
            connectAPiCallCount += 1
            let isRandom = (connectAPiCallCount > 5) ? "1" : "0"
            
            if isRandom == "1" {
                self.title = "Searching for Random User.."
            }
            else {
                self.title = "Searching For User..."
            }
            let params = ["user_id" : getUserId(),"search_with_distance" : isDistanceSelected , "subject" : subjectSelected , "distance": distanceSelected, "lat": "\(userLatitude)", "long" : "\(userLongitude)", "random" : isRandom]
            
            RequestGenerator.sharedInstance.requestData(apiName: connectApi, params: params as [String : AnyObject], delegate: self)
        }
    }
    
    func callSendChatAPI() {
    }
    
    func callReceiveChatAPI() {
        
        if ServerInterface.sharedInstance.isNetAvailable() {
        if !isBackButtonTapped {
            DispatchQueue.global().async {
                let params = ["user_id" : getUserId(), "friend_id" : self.friendId, "subject": self.subjectSelected,"max_chat_id" : self.maxChatId]
                RequestGenerator.sharedInstance.requestData(apiName: receiveChatAPi, params: params as [String : AnyObject], delegate: self, needErrorAlert : false)
            }
        }
        }
        else {
            callReceiveAPIAfterDelay()
        }
    }
    
    
    func callReceiveAPIAfterDelay() {
        
        self.perform(#selector(ChatViewController.callReceiveChatAPI), with: nil, afterDelay: 5)
        
    }
    
    func callDisconnectAPI() {
        
        SwiftLoader.show(animated: true)
        self.view.endEditing(true)
        
        let params = ["user_id" : getUserId()]
        RequestGenerator.sharedInstance.requestData(apiName: disconnectApi, params: params as [String : AnyObject], delegate: self)
        
    }
    
    func API_CALLBACK_Response(responseValue: ResponseEntity) {
        print("Response received \(responseValue.result)")
        
        if responseValue.responseCode == responseCode_1 {
            
            if responseValue.apiName == connectApi {
                if responseValue.result.count > 0 {
                    // user available for chat
                    SwiftLoader.hide()
                    
                    connectAPiCallCount = 0
                    
                    friendId = checkKeyNotAvail(dict:(responseValue.result[0] as! NSDictionary), key : "user_id") as! String
                    friendName = checkKeyNotAvail(dict:(responseValue.result[0] as! NSDictionary), key : "username") as! String
                    
                    let newSubject = checkKeyNotAvail(dict:(responseValue.result[0] as! NSDictionary), key : "subject") as! String
                    
                    if newSubject.characters.count > 0 {
                        subjectSelected = newSubject
                    }
                    
                    self.title = "Chat with \(friendName)"
                    
                    navigationItem.rightBarButtonItem = nil
                    // fetch the values from local DB and display for this friend
                    
                    if !isValueLoadedFromDB {
                        isValueLoadedFromDB = true
                        fetchMaxChatIdForUser()
//                        fetchChatFromDB()
                        self.finishReceivingMessage(animated: true)
                        
                    }
                    // start calling receive chat API
                    self.callReceiveChatAPI()
                    inputToolbar.isUserInteractionEnabled = true
                    
                }
                    
                else {
                    // user not available . Try again
                    if connectAPiCallCount < 6 {
                        self.perform(#selector(ChatViewController.callConnectAPI), with: nil, afterDelay: 5)
                    }
                    else {
                        SwiftLoader.hide()
                        // user is not available and tried 5 times. Show not available alert.
                        showNoUserAvailableAlert()
                    }
                }
            }
            else if responseValue.apiName == sendChatAPi {
                
                if responseValue.result.count > 0 {
                    //                    let dict = responseValue.result[0] as! NSDictionary
                    //                    let chatId = dict["chat_id"]
                    
                }
                // save the chat id from here
                SwiftLoader.hide()
            }
            else if responseValue.apiName == receiveChatAPi {
                // parse the result and reload the table view
                DispatchQueue.main.async {
                    
                    //                    SwiftLoader.hide()
                    if responseValue.result.count > 0 {
                        for item in responseValue.result {
                            let dict  = item as! NSDictionary
                            let chatEnt = ChatModel()
                            chatEnt.initWithDict(dict: dict)
                            let newMessage = JSQMessage(senderId: chatEnt.userId!, senderDisplayName: "", date: (chatEnt.datetime?.toDate())!, text: chatEnt.message!)
                            self.messages.append(newMessage)
                            self.maxChatId = chatEnt.chatId
                            // save on the Database
//                            self.saveChatIntoDB(chatEnt: chatEnt)
                        }
                        self.finishReceivingMessage(animated: true)
                    }

                    // check for extra value
                    
                    if responseValue.extraResponse.count > 0 { // getting extra response
                        
                        let status = checkKeyNotAvail(dict: responseValue.extraResponse , key:"status") as! String
                        let connected_user_id = checkKeyNotAvail(dict: responseValue.extraResponse , key:"connected_user_id")  as! String
                        
                        self.isUserDisconnected = false
                        self.inputToolbar.isUserInteractionEnabled = true

                        if status == "0" || (status == "1" && connected_user_id != getUserId()) {
                            // the opponenet user is diconnected with me.
                            self.title = "User has disconnected this chat."
                            self.isUserDisconnected = true
                            self.inputToolbar.isUserInteractionEnabled = false

                            
                        }
                        
                    }
                    if  !self.isUserDisconnected {
                    self.callReceiveChatAPI()
                    }
                }
                
            }
            else if responseValue.apiName == disconnectApi {
                SwiftLoader.hide()
                
                self.dismiss(animated: true, completion: nil)
                
            }
        }
            
        else {
            
            if responseValue.apiName == receiveChatAPi {
                // parse the result and reload the table view
                self.callReceiveChatAPI()
            }
            else if responseValue.apiName == connectApi {
                if connectAPiCallCount < 6 {
                    self.perform(#selector(ChatViewController.callConnectAPI), with: nil, afterDelay: 5)
                }

            }
            showValidationAlert(message: responseValue.message,presentVc: self){
                
            }

        }
    }
    func API_CALLBACK_Error(errorNumber: Int, errorMessage: String, apiName : String) {
        SwiftLoader.hide()
        if apiName == receiveChatAPi {
            // parse the result and reload the table view
            self.callReceiveChatAPI()
        }
        else if apiName == connectApi {
            if connectAPiCallCount < 6 {
                self.perform(#selector(ChatViewController.callConnectAPI), with: nil, afterDelay: 5)
            }
            
        }
        
    }
    
    func fetchChatFromDB() {
        do {
            
            let  request = NSFetchRequest<Messages>(entityName: "Messages")
            
            request.predicate =  NSPredicate(format: "((userId == %@ AND  friendId == %@) OR (userId == %@ AND  friendId == %@)) AND subject == %@ ", getUserId(), friendId, friendId, getUserId(), subjectSelected)
            
            request.returnsObjectsAsFaults = false
            
            let fetchResult =  try self.appdelegate.getContext().fetch(request)
            print(fetchResult.count)
            
            messages.removeAll()
            
            
            var fetchArray = [JSQMessage]()
            for messageEnt in fetchResult {
                
                let newMessage = JSQMessage(senderId: messageEnt.userId!, senderDisplayName: "", date: (messageEnt.datetime?.toDate())!, text: messageEnt.message!)
                fetchArray.append(newMessage)
            }
            self.messages = fetchArray.sorted {$0.date < $1.date}
        }
        catch {
            print("Fetch problem")
        }
        // sort the messages array
        
    }
    
    func saveChatIntoDB(chatEnt : ChatModel ) {
        if #available(iOS 10.0, *) {
            let messageEnt  = Messages(context : self.appdelegate.getContext())
            messageEnt.message = chatEnt.message
            messageEnt.datetime = chatEnt.datetime.toLocatStringWithFormat()
            messageEnt.chatId = chatEnt.chatId
            messageEnt.userId = chatEnt.userId
            messageEnt.friendId = chatEnt.friendId
            messageEnt.subject = subjectSelected

            self.appdelegate.saveContext()
            saveMaxIdInDB(maxChatId: messageEnt.chatId!)
            
        } else {
            // Fallback on earlier versions
            
            let messageEnt = NSEntityDescription.insertNewObject(forEntityName: "Messages", into: self.appdelegate.getContext()) as! Messages
            messageEnt.message = chatEnt.message
            messageEnt.datetime = chatEnt.datetime.toLocatStringWithFormat()
            messageEnt.chatId = chatEnt.chatId
            messageEnt.userId = chatEnt.userId
            messageEnt.friendId = chatEnt.friendId
            messageEnt.subject = subjectSelected

            self.appdelegate.saveContext()
            
            saveMaxIdInDB(maxChatId: messageEnt.chatId!)
            
        }
        
//        fetchChatFromDB()
        
        
    }
    
    func fetchMaxChatIdForUser() {
        do {
            
            let  request = NSFetchRequest<MaxChatId>(entityName: "MaxChatId")
            
            request.predicate =  NSPredicate(format: "(friendId == %@ AND subject == %@)", friendId,subjectSelected)
            request.returnsObjectsAsFaults = false
            let fetchResult =  try self.appdelegate.getContext().fetch(request)
            print(fetchResult)
            for chatEnt in fetchResult {
                maxChatId = chatEnt.maxChatId!
            }
        }
        catch {
            print("Fetch problem")
        }
        
        
    }
    
    func saveMaxIdInDB(maxChatId : String) {
        if #available(iOS 10.0, *) {
            let MaxChatIdEnt  = MaxChatId(context : self.appdelegate.getContext())
            MaxChatIdEnt.maxChatId = maxChatId
            MaxChatIdEnt.friendId = friendId
            MaxChatIdEnt.subject = subjectSelected

            self.appdelegate.saveContext()
            
        } else {
            // Fallback on earlier versions
            
            let MaxChatIdEnt = NSEntityDescription.insertNewObject(forEntityName: "MaxChatId", into: self.appdelegate.getContext()) as! MaxChatId
            MaxChatIdEnt.maxChatId = maxChatId
            MaxChatIdEnt.friendId = friendId
            MaxChatIdEnt.subject = subjectSelected

            self.appdelegate.saveContext()
        }
    }
    
    func showNoUserAvailableAlert() {
        let alertVc = UIAlertController.init(title: app_name, message: "No User available. Do you want to try again?", preferredStyle: .alert)
        
        alertVc.addAction(UIAlertAction(title: "Go Back", style: .cancel , handler: { (action) in
            
            self.backButtonTapped()
        }))
        
        
        alertVc.addAction(UIAlertAction(title: "Try Again", style: .default, handler: { (action) in
            self.connectAPiCallCount = 0
            self.callConnectAPI()
        }))
        
        
        self.present(alertVc, animated: true, completion: nil)
    }
    
    
    
    /*
     // MARK: - Navigation
     
     // In a storyboard-based application, you will often want to do a little preparation before navigation
     override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
     // Get the new view controller using segue.destinationViewController.
     // Pass the selected object to the new view controller.
     }
     */
}

