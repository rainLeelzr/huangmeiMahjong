package com.huangmei;

import com.huangmei.commomhm.CommomhmTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CommomhmTestSuite.class
})
public abstract class HuangmeiTestSuite {
}
