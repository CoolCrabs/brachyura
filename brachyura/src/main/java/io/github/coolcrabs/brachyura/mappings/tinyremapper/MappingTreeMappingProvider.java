package io.github.coolcrabs.brachyura.mappings.tinyremapper;

import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MappingTree.ClassMapping;
import net.fabricmc.mappingio.tree.MappingTree.FieldMapping;
import net.fabricmc.mappingio.tree.MappingTree.MethodArgMapping;
import net.fabricmc.mappingio.tree.MappingTree.MethodMapping;
import net.fabricmc.mappingio.tree.MappingTree.MethodVarMapping;
import net.fabricmc.tinyremapper.IMappingProvider;

public class MappingTreeMappingProvider implements IMappingProvider {
    private final MappingTree tree;
    private final int srcId;
    private final int dstId;

    public MappingTreeMappingProvider(MappingTree tree, String srcNamespace, String dstNamespace) {
        this.tree = tree;
        this.srcId = tree.getNamespaceId(srcNamespace);
        this.dstId = tree.getNamespaceId(dstNamespace);
    }

    @Override
    public void load(MappingAcceptor acceptor) {
        for (ClassMapping classMapping : tree.getClasses()) {
            String classSrcName = classMapping.getName(srcId);
            acceptor.acceptClass(classSrcName, classMapping.getName(dstId));
            for (MethodMapping method : classMapping.getMethods()) {
                Member member = new Member(
                    classSrcName,
                    method.getName(srcId),
                    method.getDesc(srcId)
                );
                acceptor.acceptMethod(
                    member,
                    method.getName(dstId)
                );
                for (MethodArgMapping methodArgMapping : method.getArgs()) {
                    acceptor.acceptMethodArg(member, methodArgMapping.getLvIndex(), methodArgMapping.getName(dstId));
                }
                for (MethodVarMapping methodVarMapping : method.getVars()) {
                    acceptor.acceptMethodVar(member, methodVarMapping.getLvIndex(), methodVarMapping.getStartOpIdx(), methodVarMapping.getLvtRowIndex(), methodVarMapping.getName(dstId));
                }
            }
            for (FieldMapping field : classMapping.getFields()) {
                acceptor.acceptField(
                    new Member(
                        classSrcName,
                        field.getName(srcId),
                        field.getDesc(srcId)
                    ),
                    field.getName(dstId)
                );
            }
        }
    }
    
}
