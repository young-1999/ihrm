package ihrm.demo;

import com.ihrm.common.entity.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ParseJwtTest {
    /**
     * 解析jwtToken字符串
     * @param
     */

    int fn(int n){
        if (n<=2){
            return 1;
        }else {
            return fn(n-1)+fn(n-2);
        }
    }




    public static void main(String[] args) {
        String token="eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4OCIsInN1YiI6IuWwj-eZvSIsImlhdCI6MTY3MjA0MDExOCwiY29tcGFueUlkIjoiMTIzNDU2IiwiY29tcGFueU5hbWUiOiLmsZ_oi4_kvKDmmbrmkq3lrqLmnInpmZDlhazlj7gifQ.Z9GJTLv8bx3CTA9qpCcteBDNBkNqgmdmNvmNo5oPo-8";
        Claims claims = Jwts.parser().setSigningKey("ihrm").parseClaimsJws(token).getBody();
        //私有对象存放在claims中
        System.out.println(claims.getId());
        System.out.println(claims.getSubject());
        System.out.println(claims.getIssuedAt());
        //解析自定义claim中的内容
        String companyId = (String)claims.get("companyId");
        String companyName = (String)claims.get("companyName");
        System.out.println(companyId);
        System.out.println(companyName);
//        ParseJwtTest test = new ParseJwtTest();
//        int n = test.fn(7);
//        System.out.println(n);

    }

    @Test
    public void sum(){
        List<Result> list=new ArrayList();
        int n=7-2;
        int x=1,y=1,m=0;
        while (n>0){
            n--;
            m=x+y;
            x=y;
            y=m;
        }
        System.out.println(m);
    }



}
