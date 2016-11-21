/*
 *
 *  Copyright 2015 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.swagger2markup.tasks

/*
 *
 *  Copyright 2015 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

import io.github.swagger2markup.markup.builder.MarkupLanguage
import io.github.swagger2markup.GroupBy
import io.github.swagger2markup.Language
import io.github.swagger2markup.OrderBy
import io.github.swagger2markup.Swagger2MarkupConverter
import io.github.swagger2markup.Swagger2MarkupConfig
import io.github.swagger2markup.builder.Swagger2MarkupConfigBuilder


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*

class Swagger2MarkupTask extends DefaultTask {
    @Input
    @Optional    
    def String swaggerInput

    @Optional
    @InputDirectory
    def File inputDir

    @Optional
    @OutputDirectory
    def File outputDir

    @Optional
    @Input
    MarkupLanguage markupLanguage;

    @Optional
    @Input
    GroupBy pathsGroupedBy;

    @Optional
    @Input
    OrderBy definitionsOrderedBy;

    @Optional
    @InputDirectory
    def File examplesDir

    @Optional
    @InputDirectory
    def File descriptionsDir

    @Optional
    @InputDirectory
    def File schemasDir

    @Optional
    @Input
    def boolean separatedDefinitions = false;

    @Optional
    @Input
    Language outputLanguage

    @Optional
    @Input
    Map<String, String> config = [:]

    @Optional
    @Input
    def File outputFile

    Swagger2MarkupTask() {
        inputDir = project.file('src/docs/swagger')
        markupLanguage = MarkupLanguage.ASCIIDOC
        outputDir = new File(project.buildDir, markupLanguage.toString().toLowerCase())
    }

    @TaskAction
    void convertSwagger2markup() {
        if (logger.isDebugEnabled()) {
            logger.debug("convertSwagger2markup task started")
            logger.debug("InputDir: {}", inputDir)
            logger.debug("Input: {}", swaggerInput)
            logger.debug("OutputDir: {}", outputDir)
            logger.debug("PathsGroupedBy: {}", pathsGroupedBy)
            logger.debug("DefinitionsOrderedBy: {}", definitionsOrderedBy)
            logger.debug("ExamplesDir: {}", examplesDir)
            logger.debug("DescriptionsDir: {}", descriptionsDir)
            logger.debug("SchemasDir: {}", schemasDir)
            logger.debug("MarkupLanguage: {}", markupLanguage)
            logger.debug("SeparatedDefinitions: {}", separatedDefinitions)
            logger.debug("OuputLanguage: {}", outputLanguage)
         }
  
        if( swaggerInput != null ){
           try{
                Swagger2MarkupConfig swagger2MarkupConfig = new Swagger2MarkupConfigBuilder(config).build();
                Swagger2MarkupConverter converter = Swagger2MarkupConverter.from(URIUtils.create(swaggerInput))
                        .withConfig(swagger2MarkupConfig).build();

                if(outputFile != null){
                    converter.toFile(outputFile.toPath());
                }else if (outputDir != null){
                    converter.toFolder(outputDir.toPath());
                }else {
                    throw new IllegalArgumentException("Either outputFile or outputDir parameter must be used");
                }
            } catch (Exception e){
                throw new IllegalArgumentException("Either outputFile or outputDir parameter must be used");
            }
        }
        inputDir.eachFile { file ->
            if (logger.isDebugEnabled()) {
                logger.debug("File: {}", file.absolutePath)
            }
            Swagger2MarkupConverter.Builder builder = Swagger2MarkupConverter.from(file.absolutePath)
                    .withMarkupLanguage(markupLanguage);
            if(pathsGroupedBy){
                builder.withPathsGroupedBy(pathsGroupedBy)
            }
            if(definitionsOrderedBy){
                builder.withDefinitionsOrderedBy(definitionsOrderedBy)
            }
            if(examplesDir){
                logger.debug("Include examples is enabled.")
                builder.withExamples(examplesDir.absolutePath)
            }
            if(descriptionsDir){
                logger.debug("Include descriptions is enabled.")
                builder.withDescriptions(descriptionsDir.absolutePath)
            }
            if(schemasDir){
                logger.debug("Include schemas is enabled.")
                builder.withSchemas(schemasDir.absolutePath)
            }
            if(separatedDefinitions){
                logger.debug("Separated definitions is enabled.")
                builder.withSeparatedDefinitions()
            }
            if(outputLanguage){
                builder.withOutputLanguage(outputLanguage)
            }
            builder.build().intoFolder(outputDir.absolutePath)
        }
        logger.debug("convertSwagger2markup task finished")
    }
}
