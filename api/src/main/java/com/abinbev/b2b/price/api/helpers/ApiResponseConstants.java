package com.abinbev.b2b.price.api.helpers;

public abstract class ApiResponseConstants {

	public static final String STATUS_200_GET_OK = "Successfully retrieved";

	public static final String STATUS_204_NO_CONTENT = "No Content";

	public static final String STATUS_400_BAD_REQUEST = "Resource is invalid";

	public static final String STATUS_403_FORBIDDEN = "Accessing the resource you were trying to reach is forbidden";

	public static final String STATUS_404_NOT_FOUND = "Not found";

	public static final String STATUS_503_SERVER_ERROR = "Service Unavailable. Please contact system administrator";

	private ApiResponseConstants() {
		// customerDealsHelper class, only constants variables here
	}
}
