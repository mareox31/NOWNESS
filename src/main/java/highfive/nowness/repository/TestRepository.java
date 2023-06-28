package highfive.nowness.repository;

import highfive.nowness.domain.Test;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TestRepository {

    List<Test> getTest();

    void addTest(Test test);

    void deleteTest(Long id);

}
