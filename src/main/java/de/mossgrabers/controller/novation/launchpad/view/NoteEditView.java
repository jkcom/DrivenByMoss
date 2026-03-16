// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.novation.launchpad.view;

import de.mossgrabers.controller.novation.launchpad.controller.LaunchpadColorManager;
import de.mossgrabers.controller.novation.launchpad.controller.LaunchpadControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.grid.IPadGrid;
import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.clip.INoteClip;
import de.mossgrabers.framework.daw.clip.IStepInfo;
import de.mossgrabers.framework.daw.clip.NotePosition;
import de.mossgrabers.framework.mode.INoteEditor;
import de.mossgrabers.framework.mode.INoteEditorMode;
import de.mossgrabers.framework.mode.NoteEditor;
import de.mossgrabers.framework.parameter.NoteAttribute;
import de.mossgrabers.framework.parameter.NoteParameter;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Edit a note from a sequencer step. Columns edit:
 * <ol>
 * <li>Velocity
 * <li>Velocity Spread
 * <li>Chance
 * <li>Gain
 * <li>Pan
 * <li>Pitch
 * <li>Recurrence
 * <li>Recurrence Pattern
 * </ol>
 *
 * @author Jürgen Moßgraber
 */
public class NoteEditView extends AbstractFaderView implements INoteEditorMode
{
    private static final int []    COLUMN_COLORS =
    {
        LaunchpadColorManager.LAUNCHPAD_COLOR_BLUE,
        LaunchpadColorManager.LAUNCHPAD_COLOR_OCEAN,
        LaunchpadColorManager.LAUNCHPAD_COLOR_PINK,
        LaunchpadColorManager.LAUNCHPAD_COLOR_BLUE,
        LaunchpadColorManager.LAUNCHPAD_COLOR_ORANGE,
        LaunchpadColorManager.LAUNCHPAD_COLOR_TURQUOISE,
        LaunchpadColorManager.LAUNCHPAD_COLOR_YELLOW,
        LaunchpadColorManager.LAUNCHPAD_COLOR_ROSE
    };

    private final boolean []       columnPan;

    private final NoteParameter [] parameters;
    private final NoteParameter    muteParameter;
    private final NoteEditor       noteEditor    = new NoteEditor ();


    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public NoteEditView (final LaunchpadControlSurface surface, final IModel model)
    {
        super ("Note Edit", surface, model);

        final IValueChanger valueChanger = model.getValueChanger ();

        this.muteParameter = new NoteParameter (NoteAttribute.MUTE, null, model, this.noteEditor, valueChanger);
        this.parameters = new NoteParameter []
        {
            new NoteParameter (NoteAttribute.VELOCITY, null, model, this.noteEditor, valueChanger),
            new NoteParameter (NoteAttribute.VELOCITY_SPREAD, null, model, this.noteEditor, valueChanger),
            new NoteParameter (NoteAttribute.CHANCE, null, model, this.noteEditor, valueChanger),
            new NoteParameter (NoteAttribute.GAIN, null, model, this.noteEditor, valueChanger),
            new NoteParameter (NoteAttribute.PANNING, null, model, this.noteEditor, valueChanger),
            new NoteParameter (NoteAttribute.TRANSPOSE, null, model, this.noteEditor, valueChanger),
            new NoteParameter (NoteAttribute.RECURRENCE_LENGTH, null, model, this.noteEditor, valueChanger),
            null
        };

        final IHost host = model.getHost ();

        this.columnPan = new boolean []
        {
            false,
            false,
            false,
            host.supports (NoteAttribute.GAIN),
            host.supports (NoteAttribute.PANNING),
            host.supports (NoteAttribute.TRANSPOSE),
            false,
            false
        };
    }


    /** {@inheritDoc} */
    @Override
    public void onDeactivate ()
    {
        super.onDeactivate ();

        // Clear the edited note
        this.noteEditor.clearNotes ();
    }


    /** {@inheritDoc} */
    @Override
    public void setupFader (final int index)
    {
        this.surface.setupFader (index, COLUMN_COLORS[index], this.columnPan[index]);

        // Prevent issue with catch mode by initializing fader value at setup
        if (index < 7)
            this.onValueKnob (index, this.getFaderValue (index));
    }


    /** {@inheritDoc} */
    @Override
    public void onValueKnob (final int index, final int value)
    {
        if (index >= 7)
            return;

        // Set immediately to prevent issue with relative scaling mode
        this.parameters[index].setValueImmediatly (value);
    }


    /** {@inheritDoc} */
    @Override
    public void onGridNote (final int note, final int velocity)
    {
        if (velocity == 0)
            return;

        final int num = note - 36;
        final int index = num % 8;
        if (index == 7)
        {
            this.toggleRecurrencePatternStep (num / 8);
            return;
        }

        if (index != 6)
        {
            super.onGridNote (note, velocity);
            return;
        }

        final int row = num / 8;
        final int value = row == 0 ? 0 : Math.min (127, (row + 1) * 16 - 1);
        this.parameters[index].setValueImmediatly (value);
    }


    /** {@inheritDoc} */
    @Override
    protected int getFaderValue (final int index)
    {
        if (index >= 7)
            return 0;

        return this.parameters[index].getValue ();
    }


    /** {@inheritDoc} */
    @Override
    public void drawGrid ()
    {
        for (int i = 0; i < 7; i++)
            this.surface.setFaderValue (i, this.parameters[i].getValue ());

        this.drawRecurrencePatternColumn ();
    }


    /** {@inheritDoc} */
    @Override
    public void onButton (final ButtonID buttonID, final ButtonEvent event, final int velocity)
    {
        if (event == ButtonEvent.DOWN && buttonID == ButtonID.SCENE1)
            this.muteParameter.setNormalizedValue (this.muteParameter.getValue () > 0 ? 0 : 1);
    }


    /** {@inheritDoc} */
    @Override
    public int getButtonColor (final ButtonID buttonID)
    {
        if (buttonID == ButtonID.SCENE1)
            return this.muteParameter.getValue () > 0 ? LaunchpadColorManager.LAUNCHPAD_COLOR_RED_HI : LaunchpadColorManager.LAUNCHPAD_COLOR_GREY_LO;

        return LaunchpadColorManager.LAUNCHPAD_COLOR_BLACK;
    }


    /** {@inheritDoc} */
    @Override
    public INoteEditor getNoteEditor ()
    {
        return this.noteEditor;
    }


    private void toggleRecurrencePatternStep (final int step)
    {
        final INoteClip clip = this.noteEditor.getClip ();
        if (clip == null)
            return;

        for (final NotePosition notePosition: this.noteEditor.getNotes ())
        {
            final IStepInfo stepInfo = clip.getStep (notePosition);
            if (!stepInfo.isRecurrenceEnabled ())
                continue;

            if (step >= stepInfo.getRecurrenceLength ())
                continue;

            clip.updateStepRecurrenceMaskToggleBit (notePosition, step);
        }
    }


    private void drawRecurrencePatternColumn ()
    {
        final IPadGrid padGrid = this.surface.getPadGrid ();
        final INoteClip clip = this.noteEditor.getClip ();
        if (clip == null || this.noteEditor.getNotes ().isEmpty ())
        {
            for (int step = 0; step < 8; step++)
                padGrid.lightEx (7, step, LaunchpadColorManager.LAUNCHPAD_COLOR_BLACK);
            return;
        }

        final IStepInfo stepInfo = clip.getStep (this.noteEditor.getNotes ().get (0));
        final boolean isEnabled = stepInfo.isRecurrenceEnabled ();
        final int recurrenceLength = isEnabled ? stepInfo.getRecurrenceLength () : 0;
        final int recurrenceMask = stepInfo.getRecurrenceMask ();

        for (int step = 0; step < 8; step++)
        {
            final boolean isActive = step < recurrenceLength;
            final boolean isOn = isActive && (recurrenceMask & 1 << step) > 0;
            final int color = !isActive ? LaunchpadColorManager.LAUNCHPAD_COLOR_BLACK : isOn ? LaunchpadColorManager.LAUNCHPAD_COLOR_ROSE : LaunchpadColorManager.LAUNCHPAD_COLOR_GREY_LO;
            padGrid.lightEx (7, 7 - step, color);
        }
    }
}
