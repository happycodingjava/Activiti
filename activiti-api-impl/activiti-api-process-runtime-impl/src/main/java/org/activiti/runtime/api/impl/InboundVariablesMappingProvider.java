/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.runtime.api.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.core.common.model.connector.ActionDefinition;
import org.activiti.core.common.model.connector.VariableDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.runtime.api.connector.InboundVariableValueProvider;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.Mapping;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.ProcessVariablesMapping;

public class InboundVariablesMappingProvider {

    private ProcessExtensionService processExtensionService;

    public InboundVariablesMappingProvider(ProcessExtensionService processExtensionService) {
        this.processExtensionService = processExtensionService;
    }

    public Object calculateMappedValue(Mapping inputMapping,
                                       DelegateExecution execution,
                                       ProcessExtensionModel extensions) {
    
        if (inputMapping != null) {
            if (Mapping.SourceMappingType.VALUE.equals(inputMapping.getType())) {
                return inputMapping.getValue();
            }
            if (Mapping.SourceMappingType.VARIABLE.equals(inputMapping.getType())) {
                String name = inputMapping.getValue().toString();
                
                return execution.getVariable(name);
                
                //This is extra check, we may agree that modeller will check everything???
//                org.activiti.spring.process.model.VariableDefinition processVariableDefinition = extensions.getExtensions().getPropertyByName(name);
//                if (processVariableDefinition != null) {
//                    return execution.getVariable(processVariableDefinition.getName());
//                }
            }
        }
        return null;
    }
    
    
    public Map<String, Object> calculateVariables(DelegateExecution execution) {
        
        Map<String, Object> inboundVariables = null;
        ProcessExtensionModel extensions = processExtensionService.getExtensionsForId(execution.getProcessDefinitionId());      
        if (extensions != null) {
            ProcessVariablesMapping processVariablesMapping = extensions.getExtensions().getMappingForFlowElement(execution.getCurrentActivityId());
            if (processVariablesMapping != null) {
                Map<String, Mapping> inputMappings = processVariablesMapping.getInputs();
               
                if (!inputMappings.isEmpty()) {
                    inboundVariables = new HashMap<>();
                    
                    for (Map.Entry<String, Mapping> mapping : inputMappings.entrySet()) {
                        Object value = calculateMappedValue(mapping.getValue(),
                                                            execution,
                                                            extensions);
                        if (value != null) {
                            inboundVariables.put(mapping.getKey(),
                                                 value);
                        }                                                                               
                    }     
                }
                
            }            
        }
       
        //Nothing found - put all process variables
        if (inboundVariables == null) {
            inboundVariables = new HashMap<>(execution.getVariables());      
        }
        
        return inboundVariables;
    }
}
