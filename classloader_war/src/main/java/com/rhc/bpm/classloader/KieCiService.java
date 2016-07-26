package com.rhc.bpm.classloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import javax.persistence.EntityManagerFactory;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.drools.persistence.jta.JtaTransactionManager;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.scanner.MavenRepository;

@Singleton
@Startup
@TransactionManagement(value = TransactionManagementType.BEAN)
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
public class KieCiService {

	@PostConstruct
	public void init() throws IOException, NamingException, NotSupportedException, SystemException, SecurityException,
			IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {

		System.out.println("I have arrived");

		RuntimeManager runtimeManagerOne = RuntimeManagerFactory.Factory.get().newPerProcessInstanceRuntimeManager(
				RuntimeEnvironmentBuilder.Factory.get().newDefaultBuilder(loadToMaven("0.0.1-SNAPSHOT")).get(), "one");

		RuntimeManager runtimeManagerTwo = RuntimeManagerFactory.Factory.get().newPerProcessInstanceRuntimeManager(
				RuntimeEnvironmentBuilder.Factory.get().newDefaultBuilder(loadToMaven("0.0.2-SNAPSHOT")).get(), "two");

		runtimeManagerOne.getRuntimeEngine(ProcessInstanceIdContext.get()).getKieSession()
				.startProcess("samples.scriptProcess");
		runtimeManagerTwo.getRuntimeEngine(ProcessInstanceIdContext.get()).getKieSession()
				.startProcess("samples.scriptProcess");
	}

	private ReleaseId loadToMaven(String version) throws IOException {
		KieFileSystem kfs = KieServices.Factory.get().newKieFileSystem();
		ReleaseId id = KieServices.Factory.get().newReleaseId("temp", "temp", version);
		kfs.generateAndWritePomXML(id);
		byte[] pom = kfs.read("pom.xml");

		InputStream kjarIs = this.getClass().getClassLoader()
				.getResourceAsStream(String.format("META-INF/processes/classloader-knowledge-%s.jar", version));

		byte[] kjar = IOUtils.toByteArray(kjarIs);

		MavenRepository.getMavenRepository().installArtifact(id, kjar, pom);

		return id;

	}

}
