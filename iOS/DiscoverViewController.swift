//
//  DiscoverViewController.swift
//  Spark
//
//  Created by Bharathi on 17/11/16.
//  Copyright © 2016 Smaat. All rights reserved.
//

import UIKit

extension DiscoverViewController : UICollectionViewDelegateFlowLayout {
    //1
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        sizeForItemAt indexPath: IndexPath) -> CGSize {
        //2
        let paddingSpace = (sectionInsets.left + sectionInsets.right)
            * CGFloat(itemsPerRow + 1)
        
        let availableWidth = collectionView.frame.width - paddingSpace
        
        
        let widthPerItem = availableWidth / itemsPerRow
        return CGSize(width: widthPerItem, height: getPixelFrom(percentage: P_cellHeight))
    }
    
    //3
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        insetForSectionAt section: Int) -> UIEdgeInsets {
        return sectionInsets
    }
    
    // 4
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return sectionInsets.top
    }
}



protocol DiscoverCellDelegate {

    func didCollapseButtonTapped(sender: UIButton)
    func didChatButtonTapped(sender: UIButton)
    func didShareButtonTapped(sender: UIButton)
}

class DiscoverViewController: BaseViewController, UICollectionViewDelegate, UICollectionViewDataSource , DiscoverCellDelegate , ServerAPIDelegate, PlayerViewDelegate {

    
    //Pixels
    
    let P_cellX = CGFloat(1.8)
    let P_cellY = CGFloat(1.2)
    let P_cellWidth = CGFloat(30.8)
    let P_cellHeight = CGFloat(17.3)
    
    //Pixels
    
    @IBOutlet weak var menuButton : UIButton?
    
    @IBOutlet weak var collectionView : UICollectionView?
    
    fileprivate  let reuseIdentifier = "DiscoverCell"
    fileprivate var sectionInsets : UIEdgeInsets!

     fileprivate let itemsPerRow: CGFloat = 3
    
    var discoverItems = [DiscoverModel]()
    
    var isFromPlayerScreen = false
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        sectionInsets = UIEdgeInsets(top: getPixelFrom(percentage: P_cellY)  , left: getPixelFrom(percentage: P_cellX, isHeight: false) / 2, bottom: getPixelFrom(percentage: P_cellY), right: getPixelFrom(percentage: P_cellX, isHeight: false) / 2 )

        
        var collectionViewFrame = collectionView?.frame
        collectionViewFrame?.size.width = SCREEN_WIDTH - ( sectionInsets.left + sectionInsets.right)
        collectionViewFrame?.origin.x = sectionInsets.left
        collectionViewFrame?.size.height = SCREEN_HEIGHT -  (collectionViewFrame?.origin.y)!
        
        collectionView?.frame = collectionViewFrame!
        collectionView?.reloadData()
        
        // Do any additional setup after loading the view.
    }
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        if !isFromPlayerScreen {
            callDiscoverAPI()
            isFromPlayerScreen = false
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func didMenuTapped() {
        onSlideMenuButtonPressed(menuButton!)
        self.tabBarController?.tabBar.layer.zPosition = -1
        self.tabBarController?.tabBar.isHidden = true
        
    }
    
    //MARK:- Collection view delegates
    func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
    }
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
                return discoverItems.count
    }
    
    func collectionView(_ collectionView: UICollectionView,
                                 cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: reuseIdentifier,
                                                      for: indexPath) as! DiscoverCustomCollectionCell
        
        let discoverModel = discoverItems[indexPath.row]
        
        cell.backgroundColor = UIColor.white
        cell.layer.cornerRadius = 5
        cell.discoverTitleLabel.text = discoverModel.discoverTopic
        cell.cellDelegate = self
        cell.getThumbanilVideoFromUrl(url: discoverModel.discoverUrl)
        cell.setTagsForButtons(tag: indexPath.row)
        
        if discoverModel.isVideoPlayed {
            cell.showTransView()
            cell.collapseButton.isSelected = true
        }
        else {
            cell.hideTransView()
            cell.collapseButton.isSelected = false
        }
    
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        
        let selectedItem = collectionView.cellForItem(at: indexPath) as! DiscoverCustomCollectionCell
        if selectedItem.isVideoCompleted  {
            selectedItem.hideTransView()
            selectedItem.collapseButton.isSelected = false // reset the collapse button
            
        }
        else {
        let playerVc = VideoPlayViewController()
            playerVc.playerDelegate = self
            playerVc.setSelectedItemToPlay(item: discoverItems[indexPath.row])
        self.present(playerVc, animated: true) { 
            }
        }
        // show in full view
    }
    
    func showShareWindow(withTitle : String = app_name, withMessage : String = "" , withUrl : String = "" ) {
        
        let activityViewController = UIActivityViewController(activityItems: [withTitle as NSString, withMessage as NSString, withUrl as NSString], applicationActivities: nil)

        if (SCREEN_TYPE !=  IPAD) {
        self.present(activityViewController, animated: true) {
        }
        }
        
        else {
            
            let popUp =  UIPopoverController(contentViewController: activityViewController)

            popUp.present(from: self.view.frame, in: self.view, permittedArrowDirections: .any, animated: true)
            
    }

    
    }
    
    //MARK:- Cell Delegate
    
    func didCollapseButtonTapped(sender : UIButton) {
        print("collapse button tapped")
    }
    func didChatButtonTapped(sender : UIButton) {
        
        let selectedtag = sender.tag - 10000
        if selectedtag < discoverItems.count && selectedtag >= 0 {
            let selectedItem = discoverItems[selectedtag]
            didChatButtonTappedFromPlayer(item: selectedItem)
              }
        
    }
    func didShareButtonTapped(sender : UIButton) {
        print("Share button tapped")
        let selectedtag = sender.tag - 20000
        if selectedtag < discoverItems.count && selectedtag >= 0 {
            let selectedItem = discoverItems[selectedtag]
            didShareButtonTappedFromPlayer(item: selectedItem)
        }

    }
    
    
    //MARK:- Player view Deelgate
    
    func didChatButtonTappedFromPlayer(item: DiscoverModel) {
        
        isFromPlayerScreen = true
        
        if ServerInterface.sharedInstance.isNetAvailable() {
            let chatVc = UIStoryboard.chatVc()
            let navVc = UINavigationController(rootViewController: chatVc)
            chatVc.distanceSelected = "0"
            chatVc.isDistanceSelected = "0"
            chatVc.subjectSelected = item.discoverTopic
            self.present(navVc, animated: true, completion: nil)
        }
        else {
            showValidationAlert(message: NO_INTERNET_AVAILABLE, presentVc: self){
            }
        }
    }
    
    func didShareButtonTappedFromPlayer(item: DiscoverModel) {
         isFromPlayerScreen = true
        showShareWindow(withTitle: app_name, withMessage: item.discoverTopic, withUrl: item.discoverUrl)

    }
    func didVideoCompletedFromPlayer(item: DiscoverModel) {
        isFromPlayerScreen = true
        _ = discoverItems.filter { (entity) -> Bool in
            if item.discoverId == entity.discoverId {
                entity.isVideoPlayed = true
                return true
            }
            else {
                entity.isVideoPlayed = false
                return false
                
            }
        }
        collectionView?.reloadData()
    }
    
    //MARK:- API calls and delegates
    
    
    func callDiscoverAPI() {
        SwiftLoader.show( animated: true)
        let params = ["user_id" : getUserId()]
        RequestGenerator.sharedInstance.requestData(apiName: getDiscoveryDetailsApi, params: params as [String : AnyObject],delegate : self)

        
    }
    
    //MARK:- Server API delegate
    func API_CALLBACK_Error(errorNumber: Int, errorMessage: String,apiName : String) {
        showValidationAlert(message: errorMessage,presentVc : self){
            
        }

        SwiftLoader.hide()
    }
    
    func API_CALLBACK_Response(responseValue: ResponseEntity) {
        SwiftLoader.hide()
        if responseValue.responseCode == responseCode_1 {
            discoverItems.removeAll()
            if responseValue.result.count > 0  {
                
                for item in responseValue.result {
                    
                    let discoverEnt = DiscoverModel()
                    discoverEnt.initWithDict(dict: item as! NSDictionary)
                    discoverItems.append(discoverEnt)
                }
            }
            collectionView?.layoutSubviews()
            collectionView?.reloadData()
        }
        else {
            showValidationAlert(message: responseValue.message, presentVc : self){
                
            }

        }
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
