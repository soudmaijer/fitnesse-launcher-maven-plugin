package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.logging.Logger;

import uk.co.javahelp.maven.plugin.fitnesse.util.FitNesseHelper;

public class FitNesseMojoTestHelper {

	AbstractFitNesseMojo mojo;
	
	ArtifactHandler artifactHandler;
	
    ArtifactResolver artifactResolver;
    
    Artifact pluginArtifact;
    
    Artifact fitnesseArtifact;
    
    Plugin plugin;
	
    ClassRealm realm;
    
    ByteArrayOutputStream logStream;
    
    boolean executeCalled = false;

    FitNesseMojoTestHelper() {
		artifactHandler = mock(ArtifactHandler.class);
		artifactResolver = mock(ArtifactResolver.class);
		realm = mock(ClassRealm.class);
		
        this.pluginArtifact = new DefaultArtifact(
        	getClass().getPackage().getName(), getClass().getSimpleName(), "version", "scope", "type", "classifier", artifactHandler);
        
        this.fitnesseArtifact = new DefaultArtifact(
            "org.fitnesse", "fitnesse", "20130530", "compile", "jar", null, artifactHandler);
        this.fitnesseArtifact.setFile(new File(getClass().getResource("/dummy.jar").getPath()));
        
		when(artifactResolver.resolve(argThat(new ResolutionRequestForArtifact(fitnesseArtifact))))
		    .thenReturn(createArtifactResolutionResult(fitnesseArtifact));
        
        this.plugin = new Plugin();
		plugin.setGroupId(pluginArtifact.getGroupId());
		plugin.setArtifactId(pluginArtifact.getArtifactId());
		
        Build build = new Build();
        build.addPlugin(plugin);
        
		mojo = new AbstractFitNesseMojo() {
			@Override
			protected void executeInternal() {
				executeCalled = true;
			}
		};
		mojo.project = new MavenProject();
		mojo.resolver = this.artifactResolver;
		mojo.fitNesseHelper = mock(FitNesseHelper.class);
		mojo.useProjectDependencies = new HashSet<String>();
		
		mojo.pluginDescriptor = new PluginDescriptor();
		mojo.pluginDescriptor.setGroupId(pluginArtifact.getGroupId());
		mojo.pluginDescriptor.setArtifactId(pluginArtifact.getArtifactId());
		mojo.pluginDescriptor.setArtifacts(Collections.singletonList(this.fitnesseArtifact));
		mojo.pluginDescriptor.setClassRealm(realm);
		mojo.project.setPluginArtifacts(Collections.singleton(this.pluginArtifact));
		mojo.project.setBuild(build);
		mojo.project.setFile(new File(getClass().getResource("pom.xml").getPath()));
		mojo.project.setArtifactId("ARTIFACT_ID");
		mojo.project.setVersion("VERSION");
		mojo.project.getBuild().setTestOutputDirectory("target/test-classes");
		mojo.project.getBuild().setOutputDirectory("target/classes");
		mojo.project.setDependencyArtifacts(new HashSet<Artifact>());
		
        addDependency("cg1", "ca1", Artifact.SCOPE_COMPILE);
        addDependency("cg1", "ca2", Artifact.SCOPE_COMPILE);
        addDependency("cg2", "ca3", Artifact.SCOPE_COMPILE);
		
        addDependency("tg1", "ta1", Artifact.SCOPE_TEST);
        addDependency("tg1", "ta2", Artifact.SCOPE_TEST);
        addDependency("tg2", "ta3", Artifact.SCOPE_TEST);
		
        addDependency("rg1", "ra1", Artifact.SCOPE_RUNTIME);
        addDependency("rg1", "ra2", Artifact.SCOPE_RUNTIME);
        addDependency("rg2", "ra3", Artifact.SCOPE_RUNTIME);
		
        addDependency("pg1", "pa1", Artifact.SCOPE_PROVIDED);
        addDependency("pg1", "pa2", Artifact.SCOPE_PROVIDED);
        addDependency("pg2", "pa3", Artifact.SCOPE_PROVIDED);
		
        addDependency("sg1", "sa1", Artifact.SCOPE_SYSTEM);
        addDependency("sg1", "sa2", Artifact.SCOPE_SYSTEM);
        addDependency("sg2", "sa3", Artifact.SCOPE_SYSTEM);
		
		logStream = new ByteArrayOutputStream();
		mojo.setLog(new DefaultLog(new PrintStreamLogger(
			Logger.LEVEL_INFO, "test", new PrintStream(logStream))));
	}
    
    private void addDependency(String groupId, String artifactId, String scope) {
        Artifact artifact = createArtifact(groupId, artifactId);
		mojo.project.getDependencies().add(createDependecy(groupId, artifactId, scope));
		mojo.project.getDependencyArtifacts().add(artifact);
		when(artifactResolver.resolve(argThat(new ResolutionRequestForArtifact(artifact))))
		    .thenReturn(createArtifactResolutionResult(artifact));
    }
	
	Dependency createDependecy(String groupId, String artifactId, String scope) {
		Dependency dependency = new Dependency();
		dependency.setGroupId(groupId);
		dependency.setArtifactId(artifactId);
		dependency.setScope(scope);
		return dependency;
	}
	
	Artifact createArtifact(String groupId, String artifactId) {
        Artifact artifact = new DefaultArtifact(
            groupId, artifactId, "1.0.0", "compile", "jar", null, artifactHandler);
        artifact.setFile(new File(getClass().getResource("/dummy.jar").getPath()));
        return artifact;
	}
	
	ArtifactResolutionResult createArtifactResolutionResult(Artifact artifact) {
	    return createArtifactResolutionResult(Collections.singleton(artifact));
	}
	
	ArtifactResolutionResult createArtifactResolutionResult(Collection<Artifact> artifacts) {
	    return createArtifactResolutionResult(artifacts, null, null);
	}
	
	ArtifactResolutionResult createArtifactResolutionResult(Collection<Artifact> artifacts, List<Artifact> missingArtifacts) {
	    return createArtifactResolutionResult(artifacts, missingArtifacts, null);
	}
	
	ArtifactResolutionResult createArtifactResolutionResult(Collection<Artifact> artifacts, ArtifactResolutionException exception) {
	    return createArtifactResolutionResult(artifacts, null, exception);
	}
	
	private ArtifactResolutionResult createArtifactResolutionResult(
			Collection<Artifact> artifacts, List<Artifact> missingArtifacts, ArtifactResolutionException exception) {
		ArtifactResolutionResult result = new ArtifactResolutionResult();
		result.setArtifacts(new HashSet<Artifact>(artifacts));
		if(missingArtifacts != null) {
			result.setUnresolvedArtifacts(missingArtifacts);
		}
		if(exception != null) {
			result.addErrorArtifactException(exception);
		}
		return result;
	}
	void classRealmAssertions() {
    	classRealmAssertions(1);
	}
	
	void classRealmAssertions(int artifactCount) {
		verify(realm, times(2 + artifactCount)).addURL(any(URL.class));
	    verify(realm, times(1)).addURL(argThat(new UrlEndsWith("/target/test-classes/")));
		verify(realm, times(1)).addURL(argThat(new UrlEndsWith("/target/classes/")));
		verify(realm, times(artifactCount)).addURL(argThat(new UrlEndsWith("/target/test-classes/dummy.jar")));
	}
}
