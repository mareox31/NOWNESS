package highfive.nowness.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Test {

    private int id;
    private String name;

    public Test(int id, String name) {
        this.id = id;
        this.name = name;
    }

    //커밋 푸시 테스트
}
