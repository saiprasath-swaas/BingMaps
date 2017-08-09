package org.bingmaps.app;

public class Constants {
	public static final String BingMapsKey = "Al98gwfICjCiCshG95oX7wpZ7B715MH450qyINYFAoCarvb9AUI2t32yjSif0q4D";
	public static final String BingSpatialQueryKey = "Al98gwfICjCiCshG95oX7wpZ7B715MH450qyINYFAoCarvb9AUI2t32yjSif0q4D";
	
	public static final String BingSpatialAccessId = "20181f26d9e94c81acdf9496133d4f23";
	public static final String BingSpatialDataSourceName = "FourthCoffeeSample";
	public static final String BingSpatialEntityTypeName = "FourthCoffeeShops";

	public static final int DefaultSearchZoomLevel = 18;
	public static final int DefaultGPSZoomLevel = 18;
	
	//Search radius used for nearby search example
	public static final double SearchRadiusKM = 10;
	
	//Minimum distance a user must move in meters before GPS location updates on map
	public static final float GPSDistanceDelta = 5F;
	
	//Minimum time that must past in ms before GPS will update users location
	public static final long GPSTimeDelta = 1000*60;
	
	//Amount of time to display splash screen as map loads in seconds.
	public static final int SplashDisplayTime = 3000;

	public static final int PERMISSION_LOCATION_REQUEST_CODE = 833;
	
	public class PanelIds{
		public static final int Splash = 0;
		public static final int About = 1;
		public static final int Map = 2;
	}
	
	public class PushpinIcons{
		public static final String Start = "file:///android_asset/startPin.png";
		public static final String End = "file:///android_asset/endPin.png";
		public static final String GPS = "file:///android_asset/rsz_marker_1.png";
		public static final String RedFlag = "file:///android_asset/pin_red_flag.png";
	}
	
	public class DataLayers{
		public static final String Route = "route";
		public static final String GPS = "gps";
		public static final String Search = "search";
	}
}
