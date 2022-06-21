package ru.runa.wf.web.ftl.component;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.commons.ftl.FormComponentSubmissionHandler;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;


public class InputDateVariable extends FormComponent implements FormComponentSubmissionHandler {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() {
        String variableName = getParameterAsString(0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String componentHtml = ViewUtil.getComponentInput(user, webHelper, variable);
        return ViewUtil.wrapInputVariable(variable, componentHtml);
    }

    @Override
    public Map<String, ? extends Object> extractVariables(Interaction interaction, VariableDefinition variableDefinition, Map<String, ?> userInput, Map<String, String> errors) throws Exception
    {
        Map<String, String> variableStorage = Maps.newHashMap();
        Object rawVariable = userInput.get(variableDefinition.getName());
        LocalDate date = LocalDate.now();
        String[] variable = (String[])rawVariable;
        String variableAndDate = date + " " + variable[0];
        variableStorage.put(getVariableNameForSubmissionProcessing(), variableAndDate);
        return variableStorage;
    }
}