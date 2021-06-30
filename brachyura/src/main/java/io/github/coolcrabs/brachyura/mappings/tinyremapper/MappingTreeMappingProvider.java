package io.github.coolcrabs.brachyura.mappings.tinyremapper;

import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MappingTree.ClassMapping;
import net.fabricmc.mappingio.tree.MappingTree.FieldMapping;
import net.fabricmc.mappingio.tree.MappingTree.MethodMapping;
import net.fabricmc.tinyremapper.IMappingProvider;

public class MappingTreeMappingProvider implements IMappingProvider {
    private final MappingTree tree;
    private final boolean srcIsSrc;
    private final String srcNamespace;
    private final boolean dstIsSrc;
    private final String dstNamespace;

    public MappingTreeMappingProvider(MappingTree tree, String srcNamespace, String dstNamespace) {
        this.tree = tree;
        this.srcNamespace = srcNamespace;
        this.srcIsSrc = tree.getSrcNamespace().equals(srcNamespace);
        this.dstNamespace = dstNamespace;
        this.dstIsSrc = tree.getSrcNamespace().equals(dstNamespace);
    }

    @Override
    public void load(MappingAcceptor acceptor) {
        int srcId = srcIsSrc ? -1 : tree.getDstNamespaces().indexOf(srcNamespace);
        int dstId = dstIsSrc ? -1 : tree.getDstNamespaces().indexOf(dstNamespace);
        for (ClassMapping classMapping : tree.getClasses()) {
            String classSrcName = srcIsSrc ? classMapping.getSrcName() : classMapping.getDstName(srcId);
            acceptor.acceptClass(classSrcName, dstIsSrc ? classMapping.getSrcName() : classMapping.getDstName(dstId));
            for (MethodMapping method : classMapping.getMethods()) {
                acceptor.acceptMethod(
                    new Member(
                        classSrcName,
                        srcIsSrc ? method.getSrcName() : method.getDstName(dstId),
                        srcIsSrc ? method.getSrcDesc() : method.getDstDesc(dstId)
                    ),
                    dstIsSrc ? method.getSrcName() : method.getDstName(dstId)
                );
            }
            for (FieldMapping field : classMapping.getFields()) {
                acceptor.acceptField(
                    new Member(
                        classSrcName,
                        srcIsSrc ? field.getSrcName() : field.getDstName(dstId),
                        srcIsSrc ? field.getSrcDesc() : field.getDstDesc(dstId)
                    ),
                    dstIsSrc ? field.getSrcName() : field.getDstName(dstId)
                );
            }
        }
    }
    
}
