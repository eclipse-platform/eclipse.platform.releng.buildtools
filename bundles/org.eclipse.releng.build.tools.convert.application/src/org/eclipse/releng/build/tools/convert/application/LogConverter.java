
package org.eclipse.releng.build.tools.convert.application;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.releng.build.tools.convert.ant.Converter;

public class LogConverter implements IApplication {

    public Object start(final IApplicationContext context) throws Exception {
        final String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        Converter.main(args);
        return IApplication.EXIT_OK;
    }

    public void stop() {
        // nothing to do
    }
}
