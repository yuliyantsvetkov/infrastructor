package io.infrastructor.cli.handlers

import com.beust.jcommander.DynamicParameter
import com.beust.jcommander.Parameter
import groovy.time.TimeCategory
import io.infrastructor.cli.settings.ApplicationSettings
import io.infrastructor.cli.validation.FileValidator

import static io.infrastructor.core.logging.ConsoleLogger.*
import static io.infrastructor.core.logging.status.TextStatusLogger.withTextStatus
import static io.infrastructor.core.utils.GroovyShellUtils.groovyShell

public class RunHandler extends LoggingAwareHandler {
    
    @DynamicParameter(names = ["-v", "-V", "--variable"])
    Map variables = [:]

    @Parameter(names = ["-f", "--file"], required = true, validateWith = FileValidator)
    List<String> files

    @Parameter(names = ["-P", "--profile"], required = false)
    String profile
    
    def description() {
        "Run specified file."
    }
    
    def options() {
        def options = super.options() 
        options << ["--file, -f" : "File to run."]
        options << ["--variable, -v, -V" : "Define a runtime variable."]
        options << ["--profile, -P"      : "Activate a settings profile."]
    }
    
    def usage() {
        ["infrastructor run -f FILE -v PROPERTY_X=VALUE_X -v PROPERTY_Y=VALUE_Y -l LOGLEVEL", 
         "infrastructor run --file FILE -v PROPERTY_X=VALUE_X -v PROPERTY_Y=VALUE_Y --log LOGLEVEL",
         "infrastructor run --file FILE -v PROPERTY_X=VALUE_X -v PROPERTY_Y=VALUE_Y --log LOGLEVEL --profile PROFILE_NAME"]
    }
    
    def execute() {
        super.execute()
        
        def timeStart = new Date()
        
        withTextStatus { status ->
            status "> initializing system settings"
            debug "Application variables: $variables"
            debug "Application profile: $profile"
        
            def settings = ApplicationSettings.systemSettings(profile)
            debug "System settings: $settings"
            settings << variables
            debug "Effective settings: $settings"
        
            status "> initializing execution engine"
            
            def shell = groovyShell(settings)
            files.each { 
                info   "${blue("running file: '$it'")}"
                status "[FILE] '$it'"
                shell.evaluate(new File(it))
                info   "${blue("file: '$it' is done")}"
            }
            
            status "> execution complete!"
        }
        
        def duration = TimeCategory.minus(new Date(), timeStart)
        printLine "\n${green('EXECUTION COMPLETE')} in $duration"
    }
}

