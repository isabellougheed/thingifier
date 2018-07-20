package uk.co.compendiumdev.thingifier.api.response;

public class ApiResponseError {
    public static String asAppropriate(String accept, String errorMessage) {

        boolean isJson=true; // default to json

        if(accept!= null && accept.endsWith("/xml")){
            isJson=false;
        }

        if(isJson){
            return ApiResponseAsJson.getErrorMessageJson(errorMessage);
        }else{
            return ApiResponseAsXml.getErrorMessageXml(errorMessage);
        }
    }
}