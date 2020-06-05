package com.architectica.socialcomponents.managers.listeners;

import com.architectica.socialcomponents.model.Project;

public interface OnProjectChangedListener {

    public void onObjectChanged(Project obj);

    public void onError(String errorText);

}
