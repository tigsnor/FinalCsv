package check;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.vdurmont.emoji.EmojiParser;

import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;

import static com.opencsv.ICSVWriter.*;

public class DataInspection {
//육안검사 후 CSV파일 최종 CSV파일 만들기
    public void inspectionStart(Connection connection) {

        try {
            Statement stmt = connection.createStatement();
            //SELECT 문을 사용할때 WHERE NOT 조건을 사용하여 WRITE_ACCOUNT(작성자계정)과 CONTACT(연락처) 둘다 없고 TITLE(제목)과 CONTENT(내용) 둘다없는 로우를 가져오지 않는다.
            //육안 검사 후 CSV파일 DB에 넣고 각자 맞는 파일 넣고 돌리면 됨
            ResultSet drugResultSet = stmt.executeQuery("SELECT * FROM eto_19_마약판매 WHERE NOT (writer_account is null AND contact is null) AND NOT (TITLE is null AND CONTENT is null)");
            putData("ETO_19", drugResultSet);

            ResultSet resultSet0 = stmt.executeQuery("SELECT * FROM eto_20_대리입금 WHERE NOT (writer_account is null AND contact is null) AND NOT (TITLE is null AND CONTENT is null)");
            putData("ETO_20", resultSet0);

            ResultSet resultSet1 = stmt.executeQuery("SELECT * FROM eto_21_미등록대부 WHERE NOT (writer_account is null AND contact is null) AND NOT (TITLE is null AND CONTENT is null)");
            putData("ETO_21", resultSet1);
            
            ResultSet resultSet2 = stmt.executeQuery("SELECT * FROM eto_22_신용정보매매 WHERE NOT (writer_account is null AND contact is null) AND NOT (TITLE is null AND CONTENT is null)");
            putData("ETO_22", resultSet2);
            
            ResultSet resultSet3 = stmt.executeQuery("SELECT * FROM eto_23_신용카드현금화 WHERE NOT (writer_account is null AND contact is null) AND NOT (TITLE is null AND CONTENT is null)");
            putData("ETO_23", resultSet3);
            
            ResultSet resultSet4 = stmt.executeQuery("SELECT * FROM eto_24_작업대출 WHERE NOT (writer_account is null AND contact is null) AND NOT (TITLE is null AND CONTENT is null)");
            putData("ETO_24", resultSet4);
            
            ResultSet resultSet5 = stmt.executeQuery("SELECT * FROM eto_25_통장매매 WHERE NOT (writer_account is null AND contact is null) AND NOT (TITLE is null AND CONTENT is null)");
            putData("ETO_25", resultSet5);

            ResultSet resultSet6 = stmt.executeQuery("SELECT * FROM eto_26_휴대폰소액결제 WHERE NOT (writer_account is null AND contact is null) AND NOT (TITLE is null AND CONTENT is null)");
            putData("ETO_26", resultSet6);


        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void putData(String dataName, ResultSet resultSet){
        try{

            List<String[]> testList = new ArrayList<>();
            String[] data = {};
            String csvname = dataName;


            while (resultSet.next()){
                if(checkUrl(resultSet.getString("url"))){
                    //contact null처리
                    String contact2 = "";
                    if (resultSet.getString("contact") != null) {
                        contact2 = resultSet.getString("contact");
                    }

                    //title이 null이면 content가 title 대체
                    String content = resultSet.getString("content");
                    String title = textLimit(resultSet.getString("title"));
                    if(title.isEmpty()){
                        title = content;
                    }

                    int seqNum = resultSet.getRow();
                    String url= resultSet.getString("url");
                    String date = resultSet.getString("write_date");
                    String time = resultSet.getString("write_time");
                    String channel = resultSet.getString("channel");
                    String name = resultSet.getString("writer_name");
                    String account = resultSet.getString("writer_account");



                    data = new String[]{String.valueOf(seqNum), url, channel, title, date, time, name, account, contact2};
                    testList.add(data);

                    try{

                    }catch (NullPointerException e){

                    }
                }else{
                }
            }
            //csv파일 만들기
            makeCsv(csvname, testList);
    //            for (int i=0; i<testList.size(); i++) {
    //                System.out.println(data[i]);
    //            }
        }catch (SQLException | IOException e){
            System.out.println(e);
        }

    }

    //여기에 csv작성파일
    public void makeCsv(String name, List<String[]> data) throws IOException {
        DateFormat dateParse = new SimpleDateFormat("yyyyMMdd");
        Date nwDate = new Date();
        String tbDate = dateParse.format(nwDate);
        String path = "C:\\Users\\e2on\\Desktop\\가공후파일\\"+name+"_"+tbDate+"_C_001";
        CSVWriter writer = new CSVWriter(new FileWriter(path+".csv"),',',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.NO_ESCAPE_CHARACTER);

//        String[] category = {"seq", "url", "channel", "title", "write_date", "write_time", "writer_name", "write_account", "contact"};
//        writer.writeNext(category);
        for(int i=0; i<data.size(); i++){
            writer.writeNext(data.get(i));
        }

        writer.close();

    }

    //글길이를 60자로 제한
    public String textLimit(String text){
        if(text.length() >= 60){
            String limitText = text.substring(0,60);
            return limitText+"...";
        }
        return text;
    }

    //url 정규식을 확인
    public Boolean checkUrl(String url){
        Pattern p = Pattern.compile("^(?:https?:\\/\\/)?(?:www\\.)?[a-zA-Z0-9./]+$");
        Matcher m = p.matcher(url);
        if  (m.matches()){
            return true;
        }
        URL u = null;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            return false;
        }
        try {
            u.toURI();
        } catch (URISyntaxException e) {
            return false;
        }
        return true;
    }
}
