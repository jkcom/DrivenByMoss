// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.novation.launchpad.view;

import java.util.ArrayList;
import java.util.List;

import de.mossgrabers.controller.novation.launchpad.controller.LaunchpadControlSurface;
import de.mossgrabers.framework.daw.clip.INoteClip;
import de.mossgrabers.framework.daw.clip.NotePosition;
import de.mossgrabers.framework.daw.clip.StepState;
import de.mossgrabers.framework.scale.Scales;


/**
 * Helper for displaying long-pressed note information in Bitwig.
 *
 * @author Jürgen Moßgraber
 */
final class LaunchpadNoteLongPressHandler
{
    private LaunchpadNoteLongPressHandler ()
    {
        // Intentionally empty
    }


    public static void showNoteNotification (final LaunchpadControlSurface surface, final int note)
    {
        surface.getDisplay ().notify ("Note: " + Scales.formatNoteAndOctave (note, -3) + " (" + note + ")");
    }


    public static void selectNote (final INoteClip clip, final int channel, final int step, final int note)
    {
        clip.selectStepContents (new NotePosition (channel, step, note), true);
    }


    public static void showStepNotesNotification (final LaunchpadControlSurface surface, final INoteClip clip, final int channel, final int step)
    {
        final NotePosition notePosition = new NotePosition (channel, step, 0);
        final List<String> notes = new ArrayList<> ();
        for (int row = 0; row < 128; row++)
        {
            notePosition.setNote (row);
            if (clip.getStep (notePosition).getState () == StepState.START)
                notes.add (Scales.formatNoteAndOctave (row, -3) + " (" + row + ")");
        }

        if (notes.isEmpty ())
            return;

        final int maxNotes = Math.min (4, notes.size ());
        final StringBuilder message = new StringBuilder ("Notes: ");
        for (int i = 0; i < maxNotes; i++)
        {
            if (i > 0)
                message.append (", ");
            message.append (notes.get (i));
        }
        if (notes.size () > maxNotes)
            message.append (" +").append (notes.size () - maxNotes);

        surface.getDisplay ().notify (message.toString ());
    }


    public static void selectStepNotes (final INoteClip clip, final int channel, final int step)
    {
        final NotePosition notePosition = new NotePosition (channel, step, 0);
        boolean clearCurrentSelection = true;
        for (int row = 0; row < 128; row++)
        {
            notePosition.setNote (row);
            if (clip.getStep (notePosition).getState () != StepState.START)
                continue;

            clip.selectStepContents (notePosition, clearCurrentSelection);
            clearCurrentSelection = false;
        }
    }
}
