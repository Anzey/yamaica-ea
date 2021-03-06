/* Copyright (C) 2013-2015 BMW Group
 * Author: Manfred Bathelt (manfred.bathelt@bmw.de)
 * Author: Juergen Gehring (juergen.gehring@bmw.de)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package de.bmw.yamaica.ea.ui.internal.franca;

import de.bmw.yamaica.common.ui.dialogs.YamaicaExportWizard;

public class ExportWizard extends YamaicaExportWizard
{
    public ExportWizard()
    {
        super("YamaicaEAFrancaExportWizard");
        setWindowTitle("Export Franca IDL Files to Enterprise Architect Project");
    }

    @Override
    public void addPages()
    {
        yamaicaWizardExportPage = new ExportPage(workbench, structuredSelection);
        addPage(yamaicaWizardExportPage);
    }
}
