package com.hanium.findplace.findplace_10.navermap;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hanium.findplace.findplace_10.fragment.ChatListFragment;
import com.nhn.android.maps.maplib.NGeoPoint;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class NaverPointToAddress extends AsyncTask<Double, Address, String>{

    //member Variables
    private final String clientId = "uNwZ0jwOscgSGh264TFc";//애플리케이션 클라이언트 아이디값";;
    private final String clientSecret = "EfU0rUlk6z";//애플리케이션 클라이언트 시크릿값";

    //constructor
    public NaverPointToAddress(){

    }


    @Override
    public String doInBackground(Double... doubles) {
        String ret = getAddressInfo(doubles[0], doubles[1]).getAddress();
        //주소 표시하기.
        ChatListFragment.myProfile.setText("현재 위치 : "+ret);
        return ret;
    }

    @Override
    protected void onProgressUpdate(Address... address){

    }

    @Override
    protected void onPostExecute(String s){
        super.onPostExecute(s);

    }


    //Methods
    public Address getAddressInfo(NGeoPoint point){
        Address retAddress = new Address();
        double longitude = point.getLongitude();
        double latitude = point.getLatitude();
        retAddress = getAddressInfo(longitude, latitude);
        return retAddress;
    }

    public Address getAddressInfo(double longitude, double latitude){
        String resultJson = null;
        Address retAddress = new Address();
        //Get AddressInfo by JSON type
        try{
            String point = longitude+","+latitude;
            String addr = URLEncoder.encode(point, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/map/reversegeocode?query="+addr;

            Log.d("mylog", "Connetcon to REstAPI 진입시작!!!!!!!!!!!!!!!!!!!!!!!");
            //connection to restAPI
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            Log.d("mylog", "Connetcon to REstAPI 진입완료!!!!!!!!!!!!!!!!!!!!!!!");

            //Reade Query
            Log.d("myLog", "getResponseCode 에서 에러가발생했나? 밑에가 없으면 여기서 에서뜬거임");
            int responseCode = con.getResponseCode();
            Log.d("myLog", "getResponseCode 에서 에러가발생했나? 엥 성공적으로 에러없이 getResponseCode 함");
            BufferedReader br;
            if(responseCode == 200){    //정상호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                Log.d("myLog", "------------------------------------------정상호출");
            }else{                      //에러발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                Log.d("myLog", "------------------------------------------에러발생");
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while((inputLine = br.readLine()) != null){
                response.append(inputLine);
            }
            br.close();
            //result
            resultJson = response.toString().replaceAll(" ", "");
            Log.d("myLog", "위치주소 : "+resultJson);

        }catch (Exception e){
            System.out.println(e);
        }finally {

        }
        Log.d("myLog", "만약 위에 2개의 로그가 안뜬다면 catch문으로 진입한거임");
        Log.d("myLog", "resultJson : "+resultJson);

        //Parse String to JsonObject
        try{
            //JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) new JSONObject(resultJson);
            JSONObject jsonResult = (JSONObject) jsonObject.get("result");
            JSONArray retItem = (JSONArray) jsonResult.get("items");

            JSONObject tmpObj = (JSONObject) retItem.get(0);

            //retAddress 클래스에 주소정보 입력함.
            retAddress.setAddress(String.valueOf(tmpObj.get("address")));
            Log.d("myLog", "getAddressInfo: "+retAddress.getAddress());

        }catch(Exception e){
            e.printStackTrace();
        }
        return retAddress;
    }

}