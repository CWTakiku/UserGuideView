# UserGuideView v1.0.0
UserGuideView let you can Guide user how to use App

this is a common userGuideView

support four kinds of highLight style (rect、circle、oval、original）and one guide layer can show multiple highLightView

you can set different tip view  String or Drawable and you can also set offset that you want

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.mrchengwenlong:UserGuideView:v1.0.1'
	}
  
  Use:
    
    userGuideView.setConfig() // set config 
  
	userGuideView.putGuideView() // it can get guide layer ,It has multiple overloaded methods 
  
	userGuideView.setUserGuideListener //get last guide layer listener or dissmiss
	
	userGuideView.putAlwaysShowView(View v,Rect r) //Always in the view layer
  
	userGuideView.startGuide()//start guide
  
	userGuideView.nextGuide()// next guide
	    
	userGuideView.cancel()// cancel guide
  

  
   
   
   
  
   
 
 
 

  
 
  
