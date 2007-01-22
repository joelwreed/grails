/*
 * Copyright 2004-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Gant script that handles the creation of Grails applications
 * 
 * @author Graeme Rocher
 *
 * @since 0.4
 */

grailsAppName = ""

Ant.property(environment:"env")   
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"    

includeTargets << new File ( "${grailsHome}/scripts/Clean.groovy" )
includeTargets << new File ( "${grailsHome}/scripts/Init.groovy" )

task ( "default" : "Creates a Grails project, including the necessary directory structure and commons files") {
   createApp()                                                      
}     

task( createApp: "The implementation task")  {
   depends( appName, createStructure, init )   
	
	createEclipseProject()
	
	println "Created Grails Application at $basedir"	
}                         

task( createEclipseProject: "Creates the Eclipse project files") {
	Ant.copy(todir:"${basedir}") {
		fileset(dir:"${grailsHome}/src/grails/templates/ide-support/eclipse",
				includes:"*.*",
				excludes:".launch")
	}   
	Ant.copy(todir:"${basedir}", file:"${grailsHome}/src/grails/build.xml") 
	Ant.copy(file:"${grailsHome}/src/grails/templates/ide-support/eclipse/.launch", 
			tofile:"${basedir}/${grailsAppName}.launch")
			
	Ant.replace(dir:"${basedir}",includes:"**/*.*", 
				token:"@grails.libs@", value:"${getGrailsLibs()}" )
	Ant.replace(dir:"${basedir}", includes:"**/*.*",
				token:"@grails.jar@", value:"${getGrailsJar()}" )
    Ant.replace(dir:"${basedir}", includes:"**/*.*",
    			token:"@grails.version@", value:"${grailsVersion}" )


    def appKey = grailsAppName.replaceAll( /\s/, '.' ).toLowerCase()

	Ant.replace(dir:"${basedir}", includes:"**/*.*",
				token:"@grails.project.name@", value:"${grailsAppName}" )
	Ant.replace(dir:"${basedir}", includes:"**/*.*",
				token:"@grails.project.key@", value:"${appKey}" )				
}
    
task ( appName : "Evaluates the application name") {
	if(!args) {
		Ant.input(message:"Application name not specified. Please enter:", 
				  addProperty:"grails.app.name")
		grailsAppName = Ant.antProject.properties."grails.app.name"
	}     
	else {
		grailsAppName = args.trim()
		if(grailsAppName.indexOf('\n') > -1)
			grailsAppName = grailsAppName.replaceAll(/\n/, " ")
	}  
	basedir = "${basedir}/${grailsAppName}"
}                                    
    

getGrailsLibs =  { 
  def result = ''
   (new File("${grailsHome}/lib")).eachFileMatch(~/.*\.jar/) { file ->
      result += "<classpathentry kind=\"var\" path=\"GRAILS_HOME/lib/${file.name}\" />\n\n"
   }
   result	
}   
getGrailsJar =  { args ->
   result = ''
   (new File("${grailsHome}/dist")).eachFileMatch(~/^grails-.*\.jar/) { file ->
      result =  file.name
   }
   result	
}