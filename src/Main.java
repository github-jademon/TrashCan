import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

import java.awt.*;

public class Main {

    // https://www.data.go.kr/data/15041640/fileData.do

    Connection conn = null;
    public String input(String s) {
        System.out.print(s + " : ");
        return new Scanner(System.in).nextLine();
    }

    public void connect() throws ClassNotFoundException, SQLException {
        // MariaDB내의 menus 데이터베이스로 접근
        Class.forName("org.mariadb.jdbc.Driver");

        String ip = ""; // 127.0.0.1
        String port_num = ""; // 3306
        String schema_name = ""; // example

        String url = "jdbc:mariadb://"+ip+":"+port_num+"/"+schema_name;
        String userid = input("userid"); // root
        String userpw = input("userpw"); // 1234

        conn = DriverManager.getConnection(url, userid, userpw);

        for(int i=1;i<10;i++) System.out.println(".");
        System.out.println("DB 연결 성공");
    }

    public void execute() {
        Statement stmt = null;
        ResultSet rs = null;

        try {
            File file = new File(".\\index.html");
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);

            connect();

            // css는 외부 파일로 저장하여 사용
            // html 작성 후 데이터베이스를 연동하여 나타낼 부분 전까지 복사·붙여넣기

            bw.write("<!DOCTYPE html>\r\n"
                    + "<html lang=\"en\">\r\n"
                    + "<head>\r\n"
                    + "  <meta charset=\"UTF-8\">\r\n"
                    + "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\r\n"
                    + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
                    + "  \r\n"
                    + "  <title>서버수행</title>\r\n"
                    + "  <style>\n"
                    + "    .customoverlay .title {display: block;width: 95%; text-align: center; background-color: #fff; padding: 10px 5px; font-size: 14px; font-weight: bold;}\n"
                    + "    .dotOverlay {position: relative; bottom: 10px; border-radius: 6px; border: 1px solid #ccc; border-bottom: 2px solid #ddd; float: left; font-size: 12px; padding: 5px; background: #fff;}\n"
                    + "    .dotOverlay:nth-of-type(n) {border: 0; box-shadow: 0px 1px 2px #888;}\n"
                    + "    .number {font-weight: bold; color: #ee6152;}\n"
                    + "    .dotOverlay:after {content: ''; position: absolute; margin-left: -6px; left: 50%; bottom: -8px; width: 11px; height: 8px; background: url('https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/vertex_white_small.png')}\n"
                    + "    .distanceInfo {position: relative; top: 5px; left: 5px; list-style: none; margin: 0;}\n"
                    + "    .distanceInfo .label {display: inline-block; width: 50px;}\n"
                    + "    .distanceInfo:after {content: none;}\n"
                    + "    button:hover {cursor: pointer;}"
                    + "</style>\n"
                    + "</head>\r\n"
                    + "<body>\r\n"
                    + "  <div style=\"margin-left: 100px;\">\n"
                    + "    <div>\n"
                    + "      <h1 style=\"margin: 30px 10px 40px;\">쓰레기통 위치</h1>\n"
                    + "    </div>\n"
                    + "    <div id=\"map\" style=\"width: 50%;  height: 80%; position: absolute; border: 1px solid #eee; box-shadow: 7px 7px 30px 8px #ccc;\"></div>\n"
                    + "      <div style=\"position: absolute; left: 58%; border: 1px solid #eee; display: inline-block; width: 35%; box-shadow: 7px 7px 30px 8px #ccc; padding: 10px\">\n"
                    + "        <h3 style=\"margin-bottom: 30px;\">가장 가까운 쓰레기통은</h3>\n"
                    + "        <p id=\"length\">지도에 자신의 위치를 클릭하세요</p>\n"
                    + "        <p style=\"margin-bottom: 50px;\" id=\"walkTime\"></p>\n"
                    + "        <a id=\"link\" href=\"\" target=\"\"><button style=\"background-color: transparent; border: 1px solid #fef01b; padding: 10px;  border-radius: 5px;\">카카오맵으로 보기</button></a>\n"
                    + "        <button style=\"background-color: transparent; border: 1px solid #aaa; padding: 10px; margin-left: 10px; border-radius: 5px;\" onclick=\"getLoc()\">내 위치 표시</button>\n"
                    + "      </div>\n"
                    + "    </div>"
                    + "  <script type=\"text/javascript\" src=\"https://dapi.kakao.com/v2/maps/sdk.js?appkey=f460ac343becaeb8bd8ded051e1ddca9\"></script>\r\n"
                    + "  <script>\r\n"
                    + "    var mapContainer = document.getElementById('map'), // 지도를 표시할 div \r\n"
                    + "        mapOption = { \r\n"
                    + "            center: new kakao.maps.LatLng(35.77455185719732, 128.431324015156), // 지도의 중심좌표\r\n"
                    + "            level: 6 // 지도의 확대 레벨\r\n"
                    + "        };\r\n"
                    + "    \r\n"
                    + "    var map = new kakao.maps.Map(mapContainer, mapOption); // 지도를 생성합니다\r\n"
                    + "    \r\n");

            stmt = conn.createStatement();

            String sql = "SELECT * FROM dalseong";

            rs = stmt.executeQuery(sql);

            int i = 0;

            ArrayList<Double> lats = new ArrayList<Double>();
            ArrayList<Double> lngs = new ArrayList<Double>();

            while (rs.next()) {
                String address = rs.getString(3);
                String detail = rs.getString(4);
                double lat = rs.getDouble(8);
                double lng = rs.getDouble(9);

                lats.add(lat);
                lngs.add(lng);

                // 한줄씩 파일 쓰기작업 실행(html 테이블 내부 행으로 출력)
                bw.write("    var marker" + i  + "_lat=" + lat + ", marker" + i  + "_lng=" + lng + ";\n"
                        + "    var marker" + i + " = new kakao.maps.Marker({\r\n"
                        + "        position: new kakao.maps.LatLng(marker" + i + "_lat, marker" + i + "_lng),\r\n"
                        + "        title:\"" + address + " " +  detail +  "\",\n"
                        + "    });\r\n"
                        + "    marker"+ i +".setMap(map);\r\n\n"
                        + "    var infowindow" + i + " = new kakao.maps.InfoWindow({\n"
                        + "        content :'<div class=\"customoverlay\">' +\n"
                        + "        '<span class=\"title\">" + address + " " + detail + "</span>' +\n"
                        + "        '</div>'\n"
                        + "    });\n"
                        + "    kakao.maps.event.addListener(marker" + i + ", 'mouseover', function() {\n"
                        + "        infowindow" + i + ".open(map, marker" + i + ");\n"
                        + "    });\n"
                        + "    kakao.maps.event.addListener(marker" + i + ", 'mouseout', function() {\n"
                        + "        infowindow" + i + ".close();\n"
                        + "    });\n\n"
                        + "    kakao.maps.event.addListener(marker" + i + ", 'click', function() {\n"
                        + "        window.open('https://map.kakao.com/link/search/" + lat + "," + lng + "', '_blank')\n"
                        + "    });\n\n");
                i++;

            }

            bw.write("    var lats = [" + lats.get(0));
            for (int j=1; j < lats.size(); j++) {
                bw.write(", " + lats.get(j));
            }
            bw.write("];\n    var lngs = [" + lngs.get(0));
            for (int j=1; j < lngs.size(); j++) {
                bw.write(", " + lngs.get(j));
            }
            bw.write("];\n");

            bw.write("    var distanceOverlay;\n"
                    + "    var imageSrc = \"https://www.pngall.com/wp-content/uploads/2017/05/Map-Marker-PNG-Picture.png\";\n"
                    + "    var imageSize = new kakao.maps.Size(40, 40);"
                    + "    var imageSrc = \"https://www.pngall.com/wp-content/uploads/2017/05/Map-Marker-PNG-Picture.png\"; "
                    + "    var imageSize = new kakao.maps.Size(40, 40); \n"
                    + "    var markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize);\n"
                    + "    var marker = new kakao.maps.Marker({ \n"
                    + "        position: map.getCenter(), \n"
                    + "        title: \"내 위치\",\n"
                    + "        image : markerImage\n"
                    + "    }); \n\n"
                    + "    marker.setMap(map);\n"
                    + "    function getTimeHTML(distance) {\n"
                    + "            var walkkTime = distance / 67 | 0;\n"
                    + "            var walkHour = '', walkMin = '';\n\n"
                    + "            if (walkkTime > 60) {\n"
                    + "                walkHour = '<span class=\"number\">' + Math.floor(walkkTime / 60) + '</span>시간 '\n"
                    + "            }\n"
                    + "            walkMin = '<span class=\"number\">' + walkkTime % 60 + '</span>분'\n\n"
                    + "            var content = '<ul class=\"dotOverlay distanceInfo\">';\n"
                    + "            content += '    <li>';\n"
                    + "            content += '        <span class=\"label\">총거리</span><span class=\"number\">' + distance + '</span>m';\n"
                    + "            content += '    </li>';\n"
                    + "            content += '    <li>';\n"
                    + "            content += '        <span class=\"label\">도보</span>' + walkHour + walkMin;\n"
                    + "            content += '    </li>';\n"
                    + "            content += '</ul>'\n\n"
                    + "            var walkTime = document.querySelector(\"#walkTime\");\n"
                    + "            walkTime.innerHTML = walkkTime + \"분 걸립니다.\";\n\n"
                    + "            return content;\n"
                    + "        }\n\n"
                    + "        function showDistance(content, position) {\n"
                    + "            if (distanceOverlay) {\n"
                    + "                distanceOverlay.setPosition(position);\n"
                    + "                distanceOverlay.setContent(content);\n"
                    + "            } else {\n"
                    + "                distanceOverlay = new kakao.maps.CustomOverlay({\n"
                    + "                    map: map,\n"
                    + "                    content: content,\n"
                    + "                    position: position,\n"
                    + "                    xAnchor: 0,\n"
                    + "                    yAnchor: 0,\n"
                    + "                    zIndex: 3\n"
                    + "                });\n"
                    + "            }\n"
                    + "        }\n\n"
                    + "        function deleteDistnce() {\n"
                    + "            if (distanceOverlay) {\n"
                    + "                distanceOverlay.setMap(null);\n"
                    + "                distanceOverlay = null;\n"
                    + "            }\n"
                    + "        }\n\n"
                    + "        let nearLine;\n"
                    + "        function handleClick(mouseEvent) {\n"
                    + "            var latlng = mouseEvent.latLng;\n"
                    + "            run(latlng);\n"
                    + "        }\n"
                    + "        function run(latlng) {\n"
                    + "            marker.setPosition(latlng);\n\n"
                    + "            deleteDistnce();\n\n"
                    + "            var min = 100000000.0;\n"
                    + "            if (nearLine) {\n"
                    + "                nearLine.setMap(null);\n"
                    + "                nearLine.setPath(null);\n"
                    + "                nearLine = null;\n"
                    + "            }\n"
                    + "            var near_num = 0;\n"
                    + "            for (var i = 0; i < 86; i++) {\n"
                    + "                nearLine = new kakao.maps.Polyline({\n"
                    + "                    map: map, // 선을 표시할 지도입니다 \n"
                    + "                    path: [], // 선을 구성하는 좌표 배열입니다 클릭한 위치를 넣어줍니다\n"
                    + "                    strokeWeight: 3, // 선의 두께입니다 \n"
                    + "                    strokeColor: '#db4040', // 선의 색깔입니다\n"
                    + "                    strokeOpacity: 0, // 선의 불투명도입니다 0에서 1 사이값이며 0에 가까울수록 투명합니다\n"
                    + "                    strokeStyle: 'solid' // 선의 스타일입니다\n"
                    + "                });\n\n"
                    + "                var path = nearLine.getPath();\n\n"
                    + "                path.push(latlng);\n"
                    + "                path.push(new kakao.maps.LatLng(lats[i], lngs[i]));\n\n"
                    + "                nearLine.setPath(path);\n"
                    + "                var length = nearLine.getLength();\n"
                    + "                if (min > length) {\n"
                    + "                    near_num = i;\n"
                    + "                    min = length;\n"
                    + "                }\n"
                    + "                if (nearLine) {\n"
                    + "                    nearLine.setMap(null);\n"
                    + "                    nearLine.setPath(null);\n"
                    + "                    nearLine = null;\n"
                    + "                }\n"
                    + "            }\n"
                    + "            if (nearLine) {\n"
                    + "                nearLine.setMap(null);\n"
                    + "                nearLine.setPath(null);\n"
                    + "                nearLine = null;\n"
                    + "            }\n\n"
                    + "            nearLine = new kakao.maps.Polyline({\n"
                    + "                map: map, // 선을 표시할 지도입니다\n"
                    + "                path: [], // 선을 구성하는 좌표 배열입니다 클릭한 위치를 넣어줍니다\n"
                    + "                strokeWeight: 3, // 선의 두께입니다\n"
                    + "                strokeColor: '#db4040', // 선의 색깔입니다\n"
                    + "                strokeOpacity: 1, // 선의 불투명도입니다 0에서 1 사이값이며 0에 가까울수록 투명합니다\n"
                    + "                strokeStyle: 'solid' // 선의 스타일입니다\n"
                    + "            });\n\n"
                    + "            var path = nearLine.getPath();\n\n"
                    + "            path.push(latlng);\n"
                    + "            path.push(new kakao.maps.LatLng(lats[near_num], lngs[near_num]));\n\n"
                    + "            nearLine.setPath(path);\n"
                    + "            min = Math.round(min);\n"
                    + "            content = getTimeHTML(min); // 커스텀오버레이에 추가될 내용입니다\n\n"
                    + "            showDistance(content, path[path.length - 2]);\n\n"
                    + "            var leng = document.querySelector(\"#length\");\n"
                    + "            var link = document.querySelector(\"#link\")\n"
                    + "            leng.innerHTML = min + \"미터 떨어져 있고\";\n"
                    + "            link.href = \"https://map.kakao.com/link/search/\" + lats[near_num] + \", \" + lngs[near_num];\n"
                    + "            link.target = \"_blank\"\n\n"
                    + "            console.log(min);\n"
                    + "            map.panTo(latlng);\n"
                    + "        }\n"
                    + "        kakao.maps.event.addListener(map, 'click', handleClick);\n"
                    + "        function getLoc() {\n"
                    + "            alert('왼쪽 상단에서 위치표시 허용을 눌러주세요')\n"
                    + "            if (navigator.geolocation) {\n"
                    + "                navigator.geolocation.getCurrentPosition(function (position) {\n"
                    + "                    var lat = position.coords.latitude, // 위도\n"
                    + "                        lon = position.coords.longitude; // 경도\n\n"
                    + "                    var locPosition = new kakao.maps.LatLng(lat, lon);\n\n"
                    + "                    marker.setPosition(locPosition)\n"
                    + "                    run(locPosition)\n"
                    + "                });\n\n"
                    + "            } else { // HTML5의 GeoLocation을 사용할 수 없을때 마커 표시 위치와 인포윈도우 내용을 설정합니다\n"
                    + "                alert(\"No GeoLocation\")\n"
                    + "            }\n"
                    + "        }"
                    + "    </script>\r\n"
                    + "    \r\n"
                    + "</body>\r\n"
                    + "</html>\r\n");

            System.out.println("파일 쓰기가 완료되었습니다.");

            bw.close();
            osw.close();
            fos.close();

            //파일쓰기 완료 후 생성한 file을 로드
            if(file.exists()) Desktop.getDesktop().open(file);

        } catch (IOException e) {
            System.out.println("파일 입출력 오류");
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로딩 실패");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null && !stmt.isClosed()) {
                    stmt.close();
                }
                if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Main().execute();
    }
}