/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package org.eclipse.ceylon.model.typechecker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NamedArgumentList extends Element implements Scope {
    
    private ParameterList parameterList;
    private List<String> argumentNames = new ArrayList<String>();
    private int id;
    private List<Declaration> members = new ArrayList<Declaration>(3);
    
    @Override
    public boolean isToplevel() {
        return false;
    }
    
    @Override
    public List<Declaration> getMembers() {
        return members;
    }
    
    @Override
    public void addMember(Declaration declaration) {
        members.add(declaration);
    }
    
    public ParameterList getParameterList() {
        return parameterList;
    }
    public void setParameterList(ParameterList parameterList) {
        this.parameterList = parameterList;
    }
    
    @Override
    public Map<String, DeclarationWithProximity> getMatchingDeclarations(Unit unit, String startingWith, int proximity, Cancellable cancellable) {
        Map<String, DeclarationWithProximity> result = super.getMatchingDeclarations(unit, startingWith, proximity+1, cancellable);
        if (getParameterList()!=null) {
            for (Parameter p: getParameterList().getParameters()) {
                if (p.getName().startsWith(startingWith) && 
                        !getArgumentNames().contains(p.getName())) {
                    result.put(p.getName(), new DeclarationWithProximity(p, this));
                }
            }
        }
        return result;
    }
    
    public List<String> getArgumentNames() {
        return argumentNames;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    @Override
    public int hashCode() {
        int ret = 17;
        ret = (31 * ret) + getContainer().hashCode();
        ret = (31 * ret) + id;
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if (obj instanceof NamedArgumentList) {
            NamedArgumentList that = (NamedArgumentList) obj;
            return id==that.id && 
                    getContainer().equals(that.getContainer());
        }
        else {
            return false;
        }
    }
    
}
