package havis.opcua.message.common;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import havis.opcua.message.common.model.MessageDeserializeTest;
import havis.opcua.message.common.model.MessageSerializeTest;
import havis.opcua.message.common.model.ParamIdTest;
import havis.opcua.message.common.model.ParamValueTest;

/*
 * Call with VM args: -Djmockit-coverage-excludes=havis\.device\.rf\.daemon\.test\..* 
 * to exclude "test" subpackage from coverage report  
 */

@RunWith(Suite.class)
@SuiteClasses({
	MessageDeserializeTest.class,
	MessageSerializeTest.class,
	ParamIdTest.class,
	ParamValueTest.class
})
public class TestSuite {

}
