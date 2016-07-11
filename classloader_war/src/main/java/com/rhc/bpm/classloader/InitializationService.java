package com.rhc.bpm.classloader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.drools.persistence.jta.JtaTransactionManager;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

@Singleton
@Startup
@TransactionManagement(value = TransactionManagementType.BEAN)
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
public class InitializationService {


	@PostConstruct
	public void init() throws IOException, NamingException, NotSupportedException, SystemException, SecurityException,
			IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		
		URL urlOne = this.getClass().getClassLoader()
				.getResource("META-INF/processes/classloader-knowledge-0.0.1-SNAPSHOT.jar");
		
		URL urlTwo = this.getClass().getClassLoader()
				.getResource("META-INF/processes/classloader-knowledge-0.0.2-SNAPSHOT.jar");
		
		
		RuntimeManager runtimeManagerOne = RuntimeManagerFactory.Factory.get()
				.newPerProcessInstanceRuntimeManager(makeRuntimeEnvironment(urlOne), "one");
		
		RuntimeManager runtimeManagerTwo = RuntimeManagerFactory.Factory.get()
				.newPerProcessInstanceRuntimeManager(makeRuntimeEnvironment(urlTwo), "two");
		
		runtimeManagerOne.getRuntimeEngine(ProcessInstanceIdContext.get()).getKieSession()
				.startProcess("samples.scriptProcess");
		runtimeManagerTwo.getRuntimeEngine(ProcessInstanceIdContext.get()).getKieSession()
		.startProcess("samples.scriptProcess");

	}
	
	public RuntimeEnvironment makeRuntimeEnvironment(URL url){
		URL[] urls = { url };
		ClassLoader tccl = Thread.currentThread().getContextClassLoader();
		
		URLClassLoader cl = URLClassLoader.newInstance(urls, tccl);

		Thread.currentThread().setContextClassLoader(cl);

		RuntimeEnvironment runtimeEnvironment = RuntimeEnvironmentBuilder.Factory.get()
				.newClasspathKmoduleDefaultBuilder().get();

		Thread.currentThread().setContextClassLoader(tccl);
		return runtimeEnvironment;
		
	}

}
