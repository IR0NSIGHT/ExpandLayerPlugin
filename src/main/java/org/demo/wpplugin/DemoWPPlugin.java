package org.demo.wpplugin;

import org.demo.wpplugin.operations.FrostedPeaks;
import org.pepsoft.worldpainter.operations.Operation;
import org.pepsoft.worldpainter.plugins.AbstractPlugin;
import org.pepsoft.worldpainter.plugins.OperationProvider;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.demo.wpplugin.Version.VERSION;

/**
 * The main plugin class. This demo combines the various providers in one plugin class. You could of course also
 * separate them out into separate plugins classes for clarity. And of course you can leave out any providers for
 * services your plugin does not provide.
 *
 * <p><strong>Note:</strong> this class is referred to from the {@code org.pepsoft.worldpainter.plugins} file, so when
 * you rename or copy it, be sure to keep that file up-to-date.
 */
@SuppressWarnings("unused") // Instantiated by WorldPainter
public class DemoWPPlugin extends AbstractPlugin implements
        OperationProvider    // Implement this to provide one or more custom operations for the Tools panel
{
    /**
     * The plugin class must have a default (public, no arguments) constructor.
     */
    public DemoWPPlugin() {
        super(NAME, VERSION);
        System.out.println("hello world i am the demo wp plugin");
    }

    // OperationProvider

    @Override
    public List<Operation> getOperations() {
        return OPERATIONS;
    }

    /**
     * Short, human-readble name of the plugin.
     */
    static final String NAME = "Frosted Peaks Plugin";
    private static final List<Operation> OPERATIONS = singletonList(new FrostedPeaks());
}
