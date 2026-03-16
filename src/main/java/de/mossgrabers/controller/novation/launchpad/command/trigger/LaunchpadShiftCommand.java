// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.novation.launchpad.command.trigger;

import de.mossgrabers.controller.novation.launchpad.LaunchpadConfiguration;
import de.mossgrabers.controller.novation.launchpad.controller.LaunchpadControlSurface;
import de.mossgrabers.framework.command.trigger.view.ToggleShiftViewCommand;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.clip.ISessionAlternative;
import de.mossgrabers.framework.featuregroup.ViewManager;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.Views;


/**
 * Launchpad-specific shift handling.
 *
 * @author Jürgen Moßgraber
 */
public class LaunchpadShiftCommand extends ToggleShiftViewCommand<LaunchpadControlSurface, LaunchpadConfiguration>
{
    /**
     * Constructor.
     *
     * @param model The model
     * @param surface The surface
     */
    public LaunchpadShiftCommand (final IModel model, final LaunchpadControlSurface surface)
    {
        super (model, surface);
    }


    /** {@inheritDoc} */
    @Override
    public void execute (final ButtonEvent event, final int velocity)
    {
        if (this.surface.isPressed (ButtonID.ALT))
            return;

        switch (event)
        {
            case DOWN:
                this.surface.setKnobSensitivityIsSlow (true);
                this.handleOnDown ();
                break;

            case LONG:
                return;

            case UP:
                this.surface.setKnobSensitivityIsSlow (false);
                this.handleUp ();
                break;
        }
    }


    private void handleOnDown ()
    {
        if (this.viewManager.isActive (Views.SHIFT))
        {
            this.viewManager.restore ();
            this.surface.setTriggerConsumed (ButtonID.SHIFT);
            this.surface.setKnobSensitivityIsSlow (false);
            return;
        }

        if (!(this.viewManager.getActive () instanceof ISessionAlternative))
            this.viewManager.setTemporary (Views.SHIFT);
    }


    private void handleUp ()
    {
        final ViewManager viewManager = this.surface.getViewManager ();

        if (viewManager.isActive (Views.SHIFT))
        {
            viewManager.restore ();
            return;
        }

        if (viewManager.getActive () instanceof ISessionAlternative)
            this.clearAlternateInteractionUsed ();
    }
}
