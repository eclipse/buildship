package org.eclipse.buildship.ui.handler;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;


public abstract class AbstractToogleStateHandler extends AbstractHandler implements IElementUpdater {

    private static final String COMMAND_TOGGLE_STATE_ID = "org.eclipse.ui.commands.toggleState";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        Object execute = doExecute(event);

        // do not toggle, if the state is not okay. And simply igore, if it is not an IStatus
        if (execute instanceof IStatus) {
            if (!((IStatus) execute).isOK()) {
                return execute;
            }
        }

        Command command = event.getCommand();
        State state = command.getState(COMMAND_TOGGLE_STATE_ID);
        Object stateValue = state.getValue();
        if (stateValue instanceof Boolean) {
            Boolean stateBoolean = (Boolean) stateValue;
            if (getToggleState() != stateBoolean.booleanValue()) {
                HandlerUtil.toggleCommandState(command);
            }
        }

        return execute;
    }

    @Override
    public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
        element.setChecked(getToggleState());
    }

    protected abstract boolean getToggleState();

    protected abstract Object doExecute(ExecutionEvent event);
}
