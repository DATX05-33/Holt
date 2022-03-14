package holt.processor.activator;

import holt.processor.DFDName;

public sealed interface Activator permits ExternalEntity, Database, Process {
    ActivatorName name();
}
