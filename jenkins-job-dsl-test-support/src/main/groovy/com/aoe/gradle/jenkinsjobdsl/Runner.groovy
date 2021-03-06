package com.aoe.gradle.jenkinsjobdsl

import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.dsl.FileJobManagement
import javaposse.jobdsl.dsl.GeneratedItems
import javaposse.jobdsl.dsl.GeneratedJob
import javaposse.jobdsl.dsl.GeneratedView
import javaposse.jobdsl.dsl.Item
import javaposse.jobdsl.dsl.ScriptRequest

import java.util.logging.Logger

/**
 * Extend FileJobManagement with support for custom defined extensions.
 */
class MyFileJobManagement extends FileJobManagement {

    ExtensionSupport extensionSupport = new ExtensionSupport(this)

    MyFileJobManagement(File root) {
        super(root)
    }

    public Node callExtension(String name,
                              Item item,
                              Class<?> contextType,
                              Object... args) {
        extensionSupport.callExtension(name, item, contextType, args)
    }

}

/**
 * Executes the given DSL scripts from the command line and generates
 * the config XMLs. Leverage FileJobManagement
 */
class Runner {
    private static final Logger LOG = Logger.getLogger(Runner.name)

    @SuppressWarnings('NoDef')
    static void main(String[] args) throws Exception {
        if (args.length == 0) {
            LOG.severe('Script name is required')
            return
        }

        File cwd = new File('.')
        URL cwdURL = cwd.toURI().toURL()



        FileJobManagement jm = new MyFileJobManagement(cwd)
        jm.parameters.putAll(System.getenv())
        System.properties.each { def key, def value ->
            jm.parameters.put(key.toString(), value.toString())
        }

        args.each { String scriptName ->
            ScriptRequest request = new ScriptRequest(scriptName, null, cwdURL, false)
            GeneratedItems generatedItems = DslScriptLoader.runDslEngine(request, jm)

            for (GeneratedJob job : generatedItems.jobs) {
                LOG.info("From $scriptName, Generated item: $job")
            }
            for (GeneratedView view : generatedItems.views) {
                LOG.info("From $scriptName, Generated view: $view")
            }
        }
    }
}
