//
//  FirstViewController.swift
//  Spark
//
//  Created by Bharathi on 17/11/16.
//  Copyright © 2016 Smaat. All rights reserved.
//

import UIKit
import CoreLocation

var userLatitude = CGFloat(0.0)
var userLongitude  = CGFloat(0.0)


extension String {
    func heightWithConstrainedWidth(width: CGFloat, font: UIFont) -> CGFloat {
        let constraintRect = CGSize(width: width, height: .greatestFiniteMagnitude)
        let boundingBox = self.boundingRect(with: constraintRect, options: .usesLineFragmentOrigin, attributes: [NSFontAttributeName: font], context: nil)
        
        return boundingBox.height
    }
    func widthWithConstrainedHeight(height: CGFloat, font: UIFont) -> CGFloat {
        let constraintRect = CGSize(width: .greatestFiniteMagnitude, height: height)
        let boundingBox = self.boundingRect(with: constraintRect, options: .usesLineFragmentOrigin, attributes: [NSFontAttributeName: font], context: nil)
        
        return boundingBox.width + 20
    }
}


extension UITextField {
    func setTextFieldLeftView(withImage : UIImage) {
        
        self.leftViewMode = .always
        let searchImage =  withImage //UIImage(named: "Search_icon.png")
        
        let frameY = ((self.frame.height) - (searchImage.size.height) / 2) / 2
        let imageView = UIImageView(frame: CGRect(x: 0, y: frameY, width: ((searchImage.size.width) / 2) + 10, height: (searchImage.size.height) / 2))
        imageView.image = searchImage
        imageView.contentMode = .scaleAspectFit
        self.leftView = imageView
    }
    
    func setBorder() {
        self.layer.borderColor = UIColorFromRGB(color: lbltextColor, alpha: 1).cgColor
        self.layer.borderWidth = 0.5
        self.layer.cornerRadius = 5
    }
    
    
}
class ConnectViewController : BaseViewController, UITextFieldDelegate , ServerAPIDelegate , CLLocationManagerDelegate {
    
    //PIXELS
    let p_ConnectButtonHeight = CGFloat(9.1)
    let p_connectButtonX = CGFloat(4.6)
    let p_BottomPadding = CGFloat(10.6)
    let p_topicTfY = CGFloat(35)
    let p_topicTfX = CGFloat(4.4)
    let p_topicTfHeight = CGFloat(8.8)
    let p_headetHeight = CGFloat(64)
    let p_distanceSiwtchY = CGFloat(6.1)
    let p_sliderY = CGFloat(6.2)

    let p_switchHeight = CGFloat(3.1)
    let p_switchWidth = CGFloat(11.0)
    let p_sliderHeight = CGFloat(3.8)
    
    //PIXELS
    @IBOutlet weak var menuButton : UIButton?
    @IBOutlet weak var headerView : UIView?
    @IBOutlet weak var trendsLbl : UILabel?
    @IBOutlet weak var searchByDistanceLbl : UILabel?

    @IBOutlet weak var topicTf : UITextField?
    @IBOutlet weak var connectButton : UIButton?
    @IBOutlet weak var kmSlider : UISlider?
    @IBOutlet weak var distanceSwitch : UIButton?
    @IBOutlet weak var distanceLabel : UILabel?
    @IBOutlet weak var trendsScroll : UIScrollView?

   var trendsArray = [TrendsModel]()
    
    let locationManager = CLLocationManager()

    override func viewDidAppear(_ animated: Bool) {
        
        super.viewDidAppear(animated)
        updateUserLocations()
        if  getUserId().characters.count == 0 {
        // show login vc
            presentLoginViewController()
        }
        else {
            callGetTrendsApi()
        }
    }
    
    func presentLoginViewController() {
        
        let loginVC = UIStoryboard.loginVc()
        let navVc = UINavigationController(rootViewController: loginVC)
        navVc.isNavigationBarHidden = true
        self.present(navVc, animated: true, completion: nil)

    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        locationManager.requestWhenInUseAuthorization()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
        updateUserLocations()
        topicTf?.setTextFieldLeftView(withImage: UIImage(named: "Search_icon.png")!)
        topicTf?.setBorder()
        UITabBar.appearance().tintColor =  UIColorFromRGB(color: ButtonBgBlueCOlor, alpha: 1)

        didLocationEnabled() // By default setting the distance switch OFF
        setFramesForControls()
    }

    
    func setFramesForControls() {
        
        var posX = getPixelFrom(percentage: p_connectButtonX,isHeight: false)
        var posWidth = SCREEN_WIDTH - (2 * posX)
        
       var posHeight = getPixelFrom(percentage: p_ConnectButtonHeight)
        
        var posY = SCREEN_HEIGHT - (getPixelFrom(percentage: p_BottomPadding) + posHeight) // padding
        
        connectButton?.frame =  CGRect(x: posX, y: posY, width: posWidth , height: posHeight)
        
        
        // trends lable
        posX = 0
        posY =  64 //(headerView?.frame.height)!
        posWidth = SCREEN_WIDTH
        posHeight = 40
        trendsLbl?.frame = CGRect(x: posX, y: posY, width: posWidth , height: posHeight)
        
        // Trends scroll
        posY += posHeight
        posHeight = getPixelFrom(percentage: p_topicTfY) - posY - 10
        trendsScroll?.frame =  CGRect(x: posX, y: posY, width: posWidth , height: posHeight)

        // Topic TF
        posX = getPixelFrom(percentage: p_topicTfX,isHeight: false)
        posHeight = getPixelFrom(percentage: p_topicTfHeight)
         posWidth = SCREEN_WIDTH - (2 * posX)
        posY = getPixelFrom(percentage: p_topicTfY)
        
        topicTf?.frame =  CGRect(x: posX, y: posY, width: posWidth , height: posHeight)
        
        topicTf?.placeholder = "Topic"
        
        // Search distance
      
        posX = getPixelFrom(percentage: p_topicTfX,isHeight: false)
        posY +=  getPixelFrom(percentage: p_distanceSiwtchY) + posHeight
        posWidth = SCREEN_WIDTH / 2
        posHeight = getPixelFrom(percentage: p_switchHeight)
        searchByDistanceLbl?.frame =  CGRect(x: posX, y: posY, width: posWidth , height: posHeight)
        
        // distnace switch
        posX = SCREEN_WIDTH / 2
        posWidth = getPixelFrom(percentage: p_switchWidth,isHeight: false)
        distanceSwitch?.frame = CGRect(x: posX, y: posY, width: posWidth , height: posHeight)
        
        // km slider
        posY += getPixelFrom(percentage: p_sliderY) + posHeight
        posX = getPixelFrom(percentage: p_topicTfX,isHeight: false)
        posWidth = SCREEN_WIDTH - (2 * posX)

        posHeight = 25
        distanceLabel?.frame = CGRect(x: posX, y: posY -  posHeight , width: posWidth , height: posHeight)
        
        posHeight = getPixelFrom(percentage: p_sliderHeight)

        kmSlider?.frame = CGRect(x: posX, y: posY, width: posWidth , height: posHeight)
    }
 
    
    func setScrollViewTrends() {
        
        for subview in (trendsScroll?.subviews)! {
            subview.removeFromSuperview()
        }
        let screenWidth = GRAPHICS.Screen_Width()
        
        print(trendsScroll?.frame.width)
        let padding = CGFloat(10)

        var posX = padding
        var posY = padding
        var itemWidth = CGFloat(50)
        let itemHeight = CGFloat(30)
        
        let labelFont = GRAPHICS.FONT_REGULAR(s: 13)
        
        for trends in trendsArray {
            
            
            itemWidth = trends.trendsTopic.widthWithConstrainedHeight(height: itemHeight, font: labelFont!)
            
            if posX  + itemWidth + padding >= (screenWidth)  {
                posX = padding
                posY += itemHeight + padding
            }

            let itemLabel = UILabel(frame: CGRect(x: posX, y: posY, width: itemWidth, height: itemHeight))
            itemLabel.text = trends.trendsTopic
            itemLabel.font = labelFont
            itemLabel.textColor = UIColorFromRGB(color: lbltextColor, alpha: 1)
            itemLabel.layer.cornerRadius = 5
            itemLabel.layer.borderWidth = 0.5
            itemLabel.textAlignment = .center
            itemLabel.layer.borderColor = UIColorFromRGB(color: lbltextColor, alpha: 1).cgColor
            itemLabel.clipsToBounds = true
            trendsScroll?.addSubview(itemLabel)
            
            itemLabel.isUserInteractionEnabled = true

            let tapGesture =  UITapGestureRecognizer(target: self, action: #selector(ConnectViewController.selectTopicFromTrends(gesture:)))
            itemLabel.addGestureRecognizer(tapGesture)
            tapGesture.numberOfTapsRequired = 1
            
            posX += itemWidth + padding
        }
        
        trendsScroll?.contentSize = CGSize(width: (screenWidth), height: posY + itemHeight + padding)
        
    }
    
    func selectTopicFromTrends(gesture : UITapGestureRecognizer) {
         let selectedLabel = gesture.view as! UILabel
        topicTf?.text = selectedLabel.text
        
        self.view.endEditing(true)
    }
    
    
    
    func updateUserLocations() {
        
        
        if CLLocationManager.locationServicesEnabled() {
            switch(CLLocationManager.authorizationStatus()) {
            case .notDetermined, .restricted, .denied:
                print("No access")
            case .authorizedAlways, .authorizedWhenInUse:
                print("Access")
                locationManager.startUpdatingLocation()
                
            }
        } else {
            print("Location services are not enabled")
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        
        if let locValue:CLLocationCoordinate2D = manager.location!.coordinate {

        userLongitude = CGFloat(locValue.longitude)
        userLatitude = CGFloat(locValue.latitude)
        locationManager.stopUpdatingLocation()
        
        if ServerInterface.sharedInstance.isNetAvailable() {

        let geoCoder = CLGeocoder()
        geoCoder.reverseGeocodeLocation(manager.location!) { (placeMarksArray, error) in
            if placeMarksArray != nil  {
                if (placeMarksArray?.count)! > 0 {
            let placemark = placeMarksArray?[0]
            saveUserLocation(userId: (placemark?.locality)!)
            }
            }
        }
        }
        }
    }
    

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }


    //MARK:- Button Handlers
    
    @IBAction func didSliderValueChanged(slider : UISlider?) {
     
        let string = String(format: "%0.2f", arguments: [(slider?.value)!])
        distanceLabel?.text = "\(string) km"
    }
    
    @IBAction func didConnectButtonTapped() {
        
        if ServerInterface.sharedInstance.isNetAvailable() {
       
        if ((topicTf?.text?.trimmingCharacters(in: CharacterSet.whitespacesAndNewlines))?.characters.count)! > 0 {
            let chatVc = UIStoryboard.chatVc()

            let navVc = UINavigationController(rootViewController: chatVc)
            
            let kmstring = String(format: "%0.2f", arguments: [(kmSlider?.value)!])

        chatVc.distanceSelected = kmstring
        chatVc.isDistanceSelected = (distanceSwitch?.isSelected)! ? "1" : "0"
        chatVc.subjectSelected = (topicTf?.text)! as String
        
        self.present(navVc, animated: true, completion: nil)
        }
        else {
            showValidationAlert(message: "Please enter the Topic!!", presentVc: self){
                
            }
        }
        }
        else {
            showValidationAlert(message: NO_INTERNET_AVAILABLE, presentVc: self){
        }
        }
    }
    
    @IBAction func didMenuTapped() {
        onSlideMenuButtonPressed(menuButton!)
        self.tabBarController?.tabBar.layer.zPosition = -1
        self.tabBarController?.tabBar.isHidden = true

    }
    
    
    @IBAction func didLocationEnabled() {
        distanceSwitch?.isSelected = !(distanceSwitch?.isSelected)!
        if (distanceSwitch?.isSelected)! {
            kmSlider?.isEnabled = true
        }
        else {
            // disable the distance label and slider
            kmSlider?.isEnabled = false
        }
    }
    
    //MARK :- Textfield Delegates

    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        
        textField .resignFirstResponder()
        return true
    }
    
    //MARK:- Server API Delegate
    
    func callGetTrendsApi() {
        
        SwiftLoader.show(animated: true)
        let params = ["user_id" : getUserId()]
        RequestGenerator.sharedInstance.requestData(apiName: getTrendsApi, params: params as [String : AnyObject],delegate : self)

    }
    
    func API_CALLBACK_Error(errorNumber: Int, errorMessage: String,apiName : String) {
        SwiftLoader.hide()
        showValidationAlert(message: errorMessage){
            
        }


    }
    func API_CALLBACK_Response(responseValue: ResponseEntity) {
        SwiftLoader.hide()
        if responseValue.apiName == getTrendsApi {
            if responseValue.responseCode == responseCode_1 {
                if responseValue.result.count > 0  {
                    trendsArray.removeAll()
                        for item in responseValue.result {
                            
                            let trendsEnt = TrendsModel()
                            trendsEnt.initWithDict(dict: item as! NSDictionary)
                            trendsArray.append(trendsEnt)
                        }
                    }
                    setScrollViewTrends()

                }
            }
        }

    

}

