package io.github.coolcrabs.fernutil;

import java.nio.file.Path;
import java.util.function.Consumer;

import io.github.coolcrabs.fernutil.FernUtil.LineNumbers;
import net.fabricmc.fernflower.api.IFabricResultSaver;

class TFFResultSaverFabric extends TFFResultSaver implements IFabricResultSaver {

    public TFFResultSaverFabric(Path out, Consumer<LineNumbers> lines) {
        super(out, lines);
    }

}
