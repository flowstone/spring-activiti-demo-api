package me.xueyao;

import me.xueyao.util.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringActivitiDemoApiApplicationTests {

    @Test
    public void testCalaTime() throws ParseException {
        Date startDate = DateUtils.parseDate("2021-02-09 10:15", "yyyy-MM-dd HH:mm");
        Date endDate = DateUtils.parseDate("2021-02-16 10:10","yyyy-MM-dd HH:mm");
        System.out.println(DateUtils.secondBetween(startDate, endDate));
    }
}
